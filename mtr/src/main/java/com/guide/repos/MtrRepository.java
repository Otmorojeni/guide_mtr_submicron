package com.guide.repos;

import com.guide.database.DatabaseHandler;
import com.guide.models.Razdel;
import com.guide.models.Podrazdel;
import com.guide.models.Material;
import com.guide.models.MaterialProperty;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class MtrRepository {

    // ==========================================================
    // 1. ПОЛУЧЕНИЕ ИЕРАРХИИ ДЕРЕВА: Раздел -> Подраздел -> Список Материалов
    // ==========================================================
    public static LinkedHashMap<Razdel, LinkedHashMap<Podrazdel, List<Material>>> getMtrTreeStructure() {
        // LinkedHashMap гарантирует, что порядок строк на экране будет ровно таким, как его вернула БД (по алфавиту)
        LinkedHashMap<Razdel, LinkedHashMap<Podrazdel, List<Material>>> treeMap = new LinkedHashMap<>();

        // Формируем три независимых SQL-запроса к схеме dbsubmicron
        String razdelSql = "SELECT id, razdel FROM dbsubmicron.skto_razdel ORDER BY razdel ASC";
        String podrazdelSql = "SELECT id, idrazdel, podrazdel, mark FROM dbsubmicron.skto_podrazdel ORDER BY podrazdel ASC";
        // Важно: выкачиваем только те газы, у которых флаг deleted равен false
        String materialSql = "SELECT * FROM dbsubmicron.mtr_material WHERE deleted = false ORDER BY name ASC";

        // try-with-resources автоматически закроет все три Statement и ResultSet, защищая от утечки памяти в СУБД
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement rStmt = conn.prepareStatement(razdelSql); ResultSet rRs = rStmt.executeQuery();
             PreparedStatement pStmt = conn.prepareStatement(podrazdelSql); ResultSet pRs = pStmt.executeQuery();
             PreparedStatement mStmt = conn.prepareStatement(materialSql); ResultSet mRs = mStmt.executeQuery()) {

            // Шаг 1.1: Читаем строки из ResultSet и превращаем их в Java-объекты Разделов
            List<Razdel> razdels = new ArrayList<>();
            while (rRs.next()) {
                razdels.add(new Razdel(rRs.getInt("id"), rRs.getString("razdel")));
            }

            // Шаг 1.2: Превращаем строки в Java-объекты Подразделов
            List<Podrazdel> podrazdels = new ArrayList<>();
            while (pRs.next()) {
                podrazdels.add(new Podrazdel(
                    pRs.getInt("id"), pRs.getInt("idrazdel"), 
                    pRs.getString("podrazdel"), pRs.getString("mark")
                ));
            }

            // Шаг 1.3: Превращаем строки в Java-объекты Материалов (Газов)
            List<Material> materials = new ArrayList<>();
            while (mRs.next()) {
                materials.add(new Material(
                    mRs.getInt("id"), mRs.getInt("idrazdel"), mRs.getInt("idpodrazdel"),
                    mRs.getString("name"), mRs.getString("mark"), mRs.getInt("idunit"),
                    mRs.getDouble("limitreserve"), mRs.getInt("storagetime"),
                    mRs.getInt("idclassclean"), mRs.getBoolean("deleted"), mRs.getString("tu")
                ));
            }

            // Шаг 1.4: Собираем три плоских списка в иерархическую «матрешку» на основе внешних ключей (FK)
            for (Razdel razdel : razdels) {
                LinkedHashMap<Podrazdel, List<Material>> podrazdelMap = new LinkedHashMap<>();
                
                for (Podrazdel podrazdel : podrazdels) {
                    // Проверяем: если подраздел ссылается на id текущего раздела
                    if (podrazdel.getIdRazdel() == razdel.getId()) {
                        List<Material> matList = new ArrayList<>();
                        
                        for (Material material : materials) {
                            // Проверяем: если материал ссылается на id текущего подраздела
                            if (material.getIdPodrazdel() == podrazdel.getId()) {
                                matList.add(material);
                            }
                        }
                        // Кладем в карту подраздел и список его дочерних материалов
                        podrazdelMap.put(podrazdel, matList);
                    }
                }
                // Привязываем раздел к собранной карте подразделов
                treeMap.put(razdel, podrazdelMap);
            }

        } catch (SQLException e) {
            showSqlErrorDialog("Ошибка при чтении дерева структуры МТР", e);
        }

        return treeMap;
    }

    // ==========================================================
    // 2. ПОЛУЧЕНИЕ ПОДРОБНЫХ ХИМ. СВОЙСТВ ПО ID МАТЕРИАЛА
    // ==========================================================
    public static MaterialProperty getPropertyByMaterialId(int materialId) {
        String sql = "SELECT * FROM dbsubmicron.mtr_himmaterproperty WHERE idmtr = ?";
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Подставляем ID выбранного материала вместо знака "?" (защита от SQL-инъекций)
            stmt.setInt(1, materialId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Если строка в базе найдена — упаковываем её в Java-объект и возвращаем
                    return new MaterialProperty(
                        rs.getInt("id"), rs.getInt("idmtr"),
                        rs.getString("physicalchemicalproperty"), rs.getString("chemicaaction"),
                        rs.getString("firesafety"), rs.getString("storageconditions"),
                        rs.getString("incompatibility"), rs.getString("pdk"),
                        rs.getInt("classrisk")
                    );
                }
            }
        } catch (SQLException e) {
            showSqlErrorDialog("Ошибка при получении свойств материала", e);
        }
        return null; // Если свойств у материала еще нет, возвращаем null (форма справа очистится)
    }

    // ==========================================================
    // 3. БЕЗОПАСНОЕ ДОБАВЛЕНИЕ (INSERT МАТЕРИАЛА + СВОЙСТВ)
    // ==========================================================
    public static int insertMaterial(Material mat, MaterialProperty prop) {
        // Подзапросы COALESCE страхуют от NOT-NULL ограничений полей idunit и idclassclean на уровне СУБД
        String matSql = "INSERT INTO dbsubmicron.mtr_material (idrazdel, idpodrazdel, name, mark, idunit, limitreserve, storagetime, idclassclean, deleted, tu) " +
                        "VALUES (?, ?, ?, ?, (SELECT COALESCE((SELECT idunit FROM dbsubmicron.mtr_material LIMIT 1), 1)), " + 
                        "CAST(? AS double precision), ?, (SELECT COALESCE((SELECT idclassclean FROM dbsubmicron.mtr_material LIMIT 1), 1)), false, ?) RETURNING id";
        
        String propSql = "INSERT INTO dbsubmicron.mtr_himmaterproperty (idmtr, physicalchemicalproperty, chemicaaction, firesafety, storageconditions, incompatibility, pdk, classrisk) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, CAST(? AS integer))";

        int generatedMaterialId = -1;

        try (Connection conn = DatabaseHandler.getConnection()) {
            // Включаем ручное управление транзакцией (ACID). Изменения не увидят другие пользователи, пока не вызовем commit()
            conn.setAutoCommit(false); 

            // Сначала вставляем сам материал
            try (PreparedStatement matStmt = conn.prepareStatement(matSql)) {
                matStmt.setInt(1, mat.getIdRazdel());
                matStmt.setInt(2, mat.getIdPodrazdel());
                matStmt.setString(3, mat.getName());
                matStmt.setString(4, mat.getMark());
                matStmt.setDouble(5, 0.0); // limitreserve по умолчанию
                matStmt.setInt(6, mat.getStorageTime());
                matStmt.setString(7, mat.getTu());

                // Так как в запросе в конце написано RETURNING id, мы читаем сгенерированный базой ID через ResultSet
                try (ResultSet rs = matStmt.executeQuery()) {
                    if (rs.next()) {
                        generatedMaterialId = rs.getInt(1);
                    }
                }
            }

            // Если материал успешно добавился и база выдала ему ID, переходим ко второй таблице
            if (generatedMaterialId != -1) {
                try (PreparedStatement propStmt = conn.prepareStatement(propSql)) {
                    // Связываем внешним ключом (idmtr) карточку химии со свежесозданным материалом
                    propStmt.setInt(1, generatedMaterialId);
                    propStmt.setString(2, prop.getPhysicalChemicalProperty());
                    propStmt.setString(3, prop.getChemicaAction());
                    propStmt.setString(4, prop.getFireSafety());
                    propStmt.setString(5, prop.getStorageConditions());
                    propStmt.setString(6, prop.getIncompatibility());
                    propStmt.setString(7, prop.getPdk());
                    propStmt.setInt(8, prop.getClassRisk());
                    
                    propStmt.executeUpdate();
                }
                // Если оба запроса прошли без ошибок — фиксируем транзакцию навсегда
                conn.commit(); 
                System.out.println("-> Успешный транзакционный INSERT. Сгенерирован ID: " + generatedMaterialId);
            } else {
                // Если ID получить не удалось — откатываем операцию, база останется чистой
                conn.rollback();
            }
        } catch (SQLException e) {
            showSqlErrorDialog("Ошибка транзакции при ДОБАВЛЕНИИ материала в БД", e);
        }
        return generatedMaterialId;
    }

    // ==========================================================
    // 4. ТРАНЗАКЦИОННОЕ ОБНОВЛЕНИЕ МАТЕРИАЛА И СВОЙСТВ (UPSERT)
    // ==========================================================
    public static void updateMaterialProperties(Material mat, MaterialProperty prop) {
        // Скрипт обновления полей базового материала по первичному ключу (id)
        String updateMatSql = "UPDATE dbsubmicron.mtr_material SET name = ?, mark = ?, tu = ?, storagetime = ? WHERE id = ?";
        
        // Скрипты для умной работы с таблицей химических характеристик
        String checkPropSql = "SELECT COUNT(*) FROM dbsubmicron.mtr_himmaterproperty WHERE idmtr = ?";
        String updatePropSql = "UPDATE dbsubmicron.mtr_himmaterproperty SET " +
                               "physicalchemicalproperty = ?, chemicaaction = ?, firesafety = ?, " +
                               "storageconditions = ?, incompatibility = ?, pdk = ?, classrisk = CAST(? AS integer) " +
                               "WHERE idmtr = ?";
        String insertPropSql = "INSERT INTO dbsubmicron.mtr_himmaterproperty " +
                               "(idmtr, physicalchemicalproperty, chemicaaction, firesafety, storageconditions, incompatibility, pdk, classrisk) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, CAST(? AS integer))";
        
        try (Connection conn = DatabaseHandler.getConnection()) {
            conn.setAutoCommit(false); // Включаем режим единой транзакции

            // Шаг 4.1: Обновляем базовые поля (Имя, ТУ, марку) в таблице mtr_material
            try (PreparedStatement matStmt = conn.prepareStatement(updateMatSql)) {
                matStmt.setString(1, mat.getName());
                matStmt.setString(2, mat.getMark());
                matStmt.setString(3, mat.getTu());
                matStmt.setInt(4, mat.getStorageTime());
                matStmt.setInt(5, mat.getId());
                matStmt.executeUpdate();
            }

            // Шаг 4.2: Проверяем, существует ли физически строка свойств для этого id материала
            boolean propExists = false;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkPropSql)) {
                checkStmt.setInt(1, mat.getId());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        propExists = rs.getInt(1) > 0;
                    }
                }
            }
            
            // Шаг 4.3: Если строка свойств есть — делаем UPDATE, если нет (старый сырой материал) — делаем ленивый INSERT
            if (propExists) {
                try (PreparedStatement stmt = conn.prepareStatement(updatePropSql)) {
                    stmt.setString(1, prop.getPhysicalChemicalProperty());
                    stmt.setString(2, prop.getChemicaAction());
                    stmt.setString(3, prop.getFireSafety());
                    stmt.setString(4, prop.getStorageConditions());
                    stmt.setString(5, prop.getIncompatibility());
                    stmt.setString(6, prop.getPdk());
                    stmt.setInt(7, prop.getClassRisk());
                    stmt.setInt(8, mat.getId());
                    stmt.executeUpdate();
                }
            } else {
                try (PreparedStatement stmt = conn.prepareStatement(insertPropSql)) {
                    stmt.setInt(1, mat.getId());
                    stmt.setString(2, prop.getPhysicalChemicalProperty());
                    stmt.setString(3, prop.getChemicaAction());
                    stmt.setString(4, prop.getFireSafety());
                    stmt.setString(5, prop.getStorageConditions());
                    stmt.setString(6, prop.getIncompatibility());
                    stmt.setString(7, prop.getPdk());
                    stmt.setInt(8, prop.getClassRisk());
                    stmt.executeUpdate();
                }
            }
            
            conn.commit(); // Транзакция успешно завершена — сохраняем изменения
            System.out.println("-> Данные МТР успешно обновлены в транзакции для ID: " + mat.getId());
            
        } catch (SQLException e) {
            showSqlErrorDialog("Ошибка при ОБНОВЛЕНИИ параметров МТР в БД", e);
        }
    }

    // ==========================================================
    // 5. МЯГКОЕ УДАЛЕНИЕ МАТЕРИАЛА (Мягкое скрытие записи)
    // ==========================================================
    public static void deleteMaterial(int materialId) {
        // В корпоративных системах строки физически не стирают (чтобы не нарушать архивные отчеты), а переводят флаг deleted в true
        String sql = "UPDATE dbsubmicron.mtr_material SET deleted = true WHERE id = ?";
        
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, materialId);
            stmt.executeUpdate();
            System.out.println("-> Материал переведен в статус 'удален' (ID: " + materialId + ")");
        } catch (SQLException e) {
            showSqlErrorDialog("Ошибка при УДАЛЕНИИ материала из БД", e);
        }
    }

    // ==========================================================
    // СЛУЖЕБНЫЙ МЕТОД: Перехват ошибок СУБД и вывод в окно JavaFX
    // ==========================================================
    private static void showSqlErrorDialog(String context, SQLException e) {
        System.err.println(context + ": " + e.getMessage());
        
        // Перенаправляем выполнение в графический поток интерфейса, чтобы окно всплыло поверх программы и она не зависла
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Ошибка СУБД PostgreSQL");
            alert.setHeaderText(context);
            alert.setContentText("Детали сообщения от СУБД:\n" + e.getMessage() + "\n\nКод состояния SQL State: " + e.getSQLState());
            alert.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
            alert.showAndWait();
        });
    }
}