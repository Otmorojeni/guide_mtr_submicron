package com.guide.views;

import com.guide.repos.MtrRepository;
import com.guide.models.*;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.LinkedHashMap;
import java.util.List;

public class MainStage {

    // Объявляем ссылки на визуальные элементы интерфейса (дерево и кнопки)
    private TreeView<Object> categoryTree;
    private Button btnAdd, btnDelete, btnRefresh, btnSave;

    // Объявляем ссылки на текстовые компоненты правой части (карточки свойств)
    private TextField txtName, txtMark, txtTu, txtStorage, txtPdk, txtClassRisk;
    private TextArea txtPhysChem, txtChemAction, txtFireSafety, txtStorageCond, txtIncompatibility;

    // Ссылки на выбранные в данный момент Java-объекты из дерева МТР
    private Material selectedMaterial = null;
    private MaterialProperty selectedProperty = null;

    // Флаги управления режимами работы экрана
    private boolean isInsertMode = false; // true — если оператор нажал кнопку "+" и вводит новый материал
    private Podrazdel targetPodrazdel = null; // Хранит папку-подраздел, в которую пойдет вставка нового газа

    public void show(Stage primaryStage) {
        // BorderPane делит экран на 5 зон (Верх, Центр, Лев, Прав, Низ)
        BorderPane root = new BorderPane();

        // ==========================================================
        // 1. ВЕРХНЯЯ ПАНЕЛЬ ИНСТРУМЕНТОВ (Toolbar)
        // ==========================================================
        HBox toolBar = new HBox(10); // Размещает элементы горизонтально с шагом в 10 пикселей
        toolBar.setPadding(new Insets(10));
        toolBar.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        
        btnAdd = new Button("➕ Добавить");
        btnDelete = new Button("❌ Удалить");
        btnRefresh = new Button("🔄 Обновить");
        
        toolBar.getChildren().addAll(btnAdd, btnDelete, btnRefresh);
        root.setTop(toolBar); // Закрепляем панель инструментов на самый верх окна

        // ==========================================================
        // 2. ГЛАВНЫЙ РАЗДЕЛИТЕЛЬ ОКНА (SplitPane)
        // ==========================================================
        SplitPane splitPane = new SplitPane();

        // --- ЛЕВАЯ ПАНЕЛЬ: Иерархическое дерево МТР ---
        VBox leftPane = new VBox();
        categoryTree = new TreeView<>();
        VBox.setVgrow(categoryTree, Priority.ALWAYS); // Дерево растягивается на всю доступную высоту слева
        leftPane.getChildren().add(categoryTree);

        // --- ПРАВАЯ ПАНЕЛЬ: Карточка параметров МТР (со скроллом) ---
        ScrollPane rightScrollPane = new ScrollPane();
        rightScrollPane.setFitToWidth(true); // Форма растягивается по ширине правой панели
        
        GridPane gridForm = new GridPane(); // Верстает элементы сеткой (колонки и строки)
        gridForm.setPadding(new Insets(15));
        gridForm.setHgap(10); // Отступ между ячейками по горизонтали
        gridForm.setVgap(10); // Отступ между ячейками по вертикали

        // Инициализируем однострочные текстовые поля ввода
        txtName = new TextField();
        txtMark = new TextField();
        txtTu = new TextField();
        txtStorage = new TextField();
        txtPdk = new TextField();
        txtClassRisk = new TextField();

        // Инициализируем многострочные области с автопереносом строк (TextAreas)
        txtPhysChem = createCustomTextArea();
        txtChemAction = createCustomTextArea();
        txtFireSafety = createCustomTextArea();
        txtStorageCond = createCustomTextArea();
        txtIncompatibility = createCustomTextArea();

        btnSave = new Button("💾 Сохранить изменения");
        btnSave.setStyle("-fx-base: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15 8 15;");

        // Раскладываем элементы по координатам сетки: gridForm.add(Компонент, Колонка, Строка)
        gridForm.add(new Label("Наименование материала*:"), 0, 0);
        gridForm.add(txtName, 1, 0);
        gridForm.add(new Label("Марка / Обозначение:"), 0, 1);
        gridForm.add(txtMark, 1, 1);
        gridForm.add(new Label("Технические условия (ТУ):"), 0, 2);
        gridForm.add(txtTu, 1, 2);
        gridForm.add(new Label("Срок хранения (мес):"), 0, 3);
        gridForm.add(txtStorage, 1, 3);
        
        // Поля характеристик
        gridForm.add(new Label("Физико-химические свойства:"), 0, 4);
        gridForm.add(txtPhysChem, 1, 4);
        gridForm.add(new Label("Химическое воздействие:"), 0, 5);
        gridForm.add(txtChemAction, 1, 5);
        gridForm.add(new Label("Пожаробезопасность:"), 0, 6);
        gridForm.add(txtFireSafety, 1, 6);
        gridForm.add(new Label("Условия хранения:"), 0, 7);
        gridForm.add(txtStorageCond, 1, 7);
        gridForm.add(new Label("Несовместимость:"), 0, 8);
        gridForm.add(txtIncompatibility, 1, 8);
        gridForm.add(new Label("ПДК (мг/м³):"), 0, 9);
        gridForm.add(txtPdk, 1, 9);
        gridForm.add(new Label("Класс опасности:"), 0, 10);
        gridForm.add(txtClassRisk, 1, 10);

        gridForm.add(btnSave, 1, 11); // Сажаем кнопку сохранения в самый низ сетки
        rightScrollPane.setContent(gridForm);

        // Помещаем левую панель и правую прокручиваемую форму в общий сплиттер
        splitPane.getItems().addAll(leftPane, rightScrollPane);
        splitPane.setDividerPositions(0.35); // 35% экрана отдаем под дерево категорий слева
        root.setCenter(splitPane);

        // ==========================================================
        // 3. ЛОГИКА ВЗАИМОДЕЙСТВИЯ И ОБРАБОТЧИКИ СОБЫТИЙ (CRUD)
        // ==========================================================

        // А. Слушатель клика по элементам дерева TreeView
        categoryTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // Защита: если мы сейчас пишем новый материал — игнорируем клики по дереву, чтобы не сбросить ввод
            if (isInsertMode) return;

            // Если оператор кликнул на конечный узел, который является объектом класса Material
            if (newValue != null && newValue.getValue() instanceof Material) {
                selectedMaterial = (Material) newValue.getValue();
                
                // Идем в репозиторий и выкачиваем детальные свойства по ID материала
                selectedProperty = MtrRepository.getPropertyByMaterialId(selectedMaterial.getId());
                
                // Переносим текст из объектов в текстовые поля карточки справа
                fillForm(selectedMaterial, selectedProperty);
            } else {
                // Если кликнули по корню или по папке раздела — очищаем переменные и карточку параметров
                selectedMaterial = null;
                selectedProperty = null;
                clearForm();
            }
        });

        // Б. Кнопка активации режима добавления нового материала (➕)
        btnAdd.setOnAction(e -> {
            TreeItem<Object> selectedItem = categoryTree.getSelectionModel().getSelectedItem();
            
            // Проверяем: добавить газ можно только в том случае, если в дереве выбрана папка-подраздел
            if (selectedItem != null && selectedItem.getValue() instanceof Podrazdel) {
                targetPodrazdel = (Podrazdel) selectedItem.getValue();
                isInsertMode = true; // Блокируем чтение других веток
                
                clearForm(); // Чистим форму для ввода новых параметров
                txtName.setPromptText("Введите наименование нового МТР...");
                txtName.requestFocus(); // Переносим курсор в поле названия
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION, 
                    "Режим добавления активен. Заполните карточку справа для подраздела [" + targetPodrazdel.getName() + "] и нажмите 'Сохранить изменения'.", ButtonType.OK);
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, 
                    "Для добавления нового материала сначала выберите конкретный ПОДРАЗДЕЛ в дереве слева!", ButtonType.OK);
                alert.showAndWait();
            }
        });

        // В. Единая кнопка сохранения данных (💾) — выполняет и INSERT, и UPDATE
        btnSave.setOnAction(e -> {
            // --- СЦЕНАРИЙ 1: Сохранение абсолютно нового материала (INSERT) ---
            if (isInsertMode && targetPodrazdel != null) {
                if (txtName.getText().trim().isEmpty()) {
                    new Alert(Alert.AlertType.ERROR, "Поле 'Наименование материала' обязательно для заполнения!", ButtonType.OK).showAndWait();
                    return;
                }

                // Собираем объект Material на базе введенных строк. Первичный ключ передаем как 0 (БД перепишет его через SERIAL)
                Material newMat = new Material(
                    0, 
                    targetPodrazdel.getIdRazdel(), 
                    targetPodrazdel.getId(), 
                    txtName.getText().trim(), 
                    txtMark.getText().trim(), 
                    1, 0.0, 
                    txtStorage.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtStorage.getText().trim()), 
                    1, false, 
                    txtTu.getText().trim()
                );

                // Собираем объект MaterialProperty для второй таблицы характеристик
                MaterialProperty newProp = new MaterialProperty(
                    0, 0, 
                    txtPhysChem.getText().trim(),
                    txtChemAction.getText().trim(),
                    txtFireSafety.getText().trim(),
                    txtStorageCond.getText().trim(),
                    txtIncompatibility.getText().trim(),
                    txtPdk.getText().trim(),
                    txtClassRisk.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtClassRisk.getText().trim())
                );

                // Командуем репозиторию запустить транзакцию добавления
                MtrRepository.insertMaterial(newMat, newProp);

                // Сбрасываем выделение и состояние режима добавления
                categoryTree.getSelectionModel().clearSelection();
                isInsertMode = false;
                targetPodrazdel = null;

                // Перерисовываем дерево считыванием свежих строк из СУБД и чистим форму
                loadTreeData();
                clearForm();

                new Alert(Alert.AlertType.INFORMATION, "Материал успешно сохранен в базу данных!", ButtonType.OK).showAndWait();

            // --- СЦЕНАРИЙ 2: Модификация параметров существующего в базе МТР (UPDATE) ---
            } else if (selectedMaterial != null) {
                
                // Переносим текст из формы экрана в текущий объект материала
                selectedMaterial.setName(txtName.getText().trim());
                selectedMaterial.setMark(txtMark.getText().trim());
                selectedMaterial.setTu(txtTu.getText().trim());
                try {
                    String storageText = txtStorage.getText().trim();
                    selectedMaterial.setStorageTime(storageText.isEmpty() ? 0 : Integer.parseInt(storageText));
                } catch (NumberFormatException ex) {
                    selectedMaterial.setStorageTime(0);
                }

                // Если у выбранного старого материала раньше не было свойств — лениво инициализируем их пустой коробкой
                if (selectedProperty == null) {
                    selectedProperty = new MaterialProperty(0, selectedMaterial.getId(), "", "", "", "", "", "", 0);
                }

                // Переносим текст из текстовых полей экрана в объект свойств
                selectedProperty.setPhysicalChemicalProperty(txtPhysChem.getText().trim());
                selectedProperty.setChemicaAction(txtChemAction.getText().trim());
                selectedProperty.setFireSafety(txtFireSafety.getText().trim());
                selectedProperty.setStorageConditions(txtStorageCond.getText().trim());
                selectedProperty.setIncompatibility(txtIncompatibility.getText().trim());
                selectedProperty.setPdk(txtPdk.getText().trim());
                try {
                    String riskText = txtClassRisk.getText().trim();
                    selectedProperty.setClassRisk(riskText.isEmpty() ? 0 : Integer.parseInt(riskText));
                } catch (NumberFormatException ex) {
                    selectedProperty.setClassRisk(0);
                }

                // Командуем репозиторию обновить данные в СУБД в рамках транзакции UPSERT
                MtrRepository.updateMaterialProperties(selectedMaterial, selectedProperty);
                
                // Сбрасываем кэш выделения дерева, заново перестраиваем его из БД и чистим поля формы
                categoryTree.getSelectionModel().clearSelection();
                loadTreeData();
                clearForm();
                
                new Alert(Alert.AlertType.INFORMATION, "Параметры материала успешно изменены в базе данных!", ButtonType.OK).showAndWait();
            } else {
                new Alert(Alert.AlertType.WARNING, "Выберите материал из дерева слева или нажмите кнопку '+ Добавить'", ButtonType.OK).showAndWait();
            }
        });

        // Г. Кнопка мягкого удаления (❌) — переводит флаг deleted в true
        btnDelete.setOnAction(e -> {
            if (selectedMaterial != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Вы действительно хотите удалить материал [" + selectedMaterial.getName() + "]?", ButtonType.YES, ButtonType.NO);
                confirm.showAndWait();
                
                if (confirm.getResult() == ButtonType.YES) {
                    // Ставим флаг deleted = true в базе данных через репозиторий
                    MtrRepository.deleteMaterial(selectedMaterial.getId());
                    
                    // Обнуляем экран и заново перерисовываем дерево ресурсов
                    categoryTree.getSelectionModel().clearSelection();
                    loadTreeData();
                    clearForm();
                }
            } else {
                new Alert(Alert.AlertType.WARNING, "Пожалуйста, выберите в дереве элемент для удаления!", ButtonType.OK).showAndWait();
            }
        });

        // Д. Кнопка ручного обновления дерева структуры (🔄)
        btnRefresh.setOnAction(e -> {
            isInsertMode = false;
            targetPodrazdel = null;
            categoryTree.getSelectionModel().clearSelection();
            loadTreeData();
            clearForm();
        });

        // Стартовая загрузка данных при первом открытии окна АИС
        loadTreeData();

        // Настройка графического контейнера сцены и запуск отображения окна
        Scene scene = new Scene(root, 1150, 800);
        primaryStage.setTitle("Справочник МТР — АИС Субмикрон (Чистый JavaFX)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Метод построения иерархических узлов дерева (Раздел -> Подраздел -> Материал)
    private void loadTreeData() {
        // Защита от кэширования: если дерево уже содержало узлы — принудительно стираем их из памяти UI перед заливкой новых
        if (categoryTree != null && categoryTree.getRoot() != null) {
            categoryTree.getRoot().getChildren().clear();
            categoryTree.setRoot(null);
        }

        // Создаем главный невидимый корень дерева
        TreeItem<Object> rootNode = new TreeItem<>("Материально-технические ресурсы");
        rootNode.setExpanded(true);

        // Вызываем репозиторий для получения LinkedHashMap структуры, собранной из БД
        LinkedHashMap<Razdel, LinkedHashMap<Podrazdel, List<Material>>> data = MtrRepository.getMtrTreeStructure();

        // Пробегаем по матрешке коллекций и строим иерархию узлов TreeItem
        data.forEach((razdel, podrazdels) -> {
            TreeItem<Object> razdelItem = new TreeItem<>(razdel);
            
            podrazdels.forEach((podrazdel, materials) -> {
                TreeItem<Object> podrazdelItem = new TreeItem<>(podrazdel);
                
                for (Material mat : materials) {
                    TreeItem<Object> materialItem = new TreeItem<>(mat);
                    podrazdelItem.getChildren().add(materialItem); // Добавляем материал в подраздел
                }
                
                razdelItem.getChildren().add(podrazdelItem); // Добавляем подраздел в раздел
            });

            rootNode.getChildren().add(razdelItem); // Добавляем раздел в корень
        });

        categoryTree.setRoot(rootNode);

        // Кастомная фабрика ячеек: учит JavaFX вызывать методы .getName() у разных классов вместо вывода пустых хэшей объектов
        categoryTree.setCellFactory(tv -> new TreeCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item instanceof String) {
                    setText((String) item);
                } else if (item instanceof Razdel) {
                    setText(((Razdel) item).getName());
                } else if (item instanceof Podrazdel) {
                    setText(((Podrazdel) item).getName());
                } else if (item instanceof Material) {
                    setText(((Material) item).getName());
                }
            }
        });
    }

    // Вспомогательный служебный метод заполнения текстовых полей формы данными выбранного из дерева газа
    private void fillForm(Material mat, MaterialProperty prop) {
        txtName.setText(mat.getName());
        txtMark.setText(mat.getMark());
        txtTu.setText(mat.getTu());
        txtStorage.setText(String.valueOf(mat.getStorageTime()));

        if (prop != null) {
            txtPhysChem.setText(prop.getPhysicalChemicalProperty());
            txtChemAction.setText(prop.getChemicaAction());
            txtFireSafety.setText(prop.getFireSafety());
            txtStorageCond.setText(prop.getStorageConditions());
            txtIncompatibility.setText(prop.getIncompatibility());
            txtPdk.setText(prop.getPdk());
            txtClassRisk.setText(String.valueOf(prop.getClassRisk()));
        } else {
            // Если расширенных свойств в базе под этот материал пока не создано — очищаем текстовые поля
            txtPhysChem.clear(); txtChemAction.clear(); txtFireSafety.clear();
            txtStorageCond.clear(); txtIncompatibility.clear(); txtPdk.clear(); txtClassRisk.clear();
        }
    }

    // Вспомогательный служебный метод полной очистки полей формы справа
    private void clearForm() {
        txtName.clear(); txtMark.clear(); txtTu.clear(); txtStorage.clear();
        txtPhysChem.clear(); txtChemAction.clear(); txtFireSafety.clear();
        txtStorageCond.clear(); txtIncompatibility.clear(); txtPdk.clear(); txtClassRisk.clear();
        txtName.setPromptText("");
    }

    // Фабрика конфигурирования TextArea для стандартизации разметки полей ввода химии
    private TextArea createCustomTextArea() {
        TextArea area = new TextArea();
        area.setPrefRowCount(3); // Ограничиваем высоту текстовой ячейки карточки до 3 строк для компактности
        area.setWrapText(true);   // Включаем автоматический перенос длинных слов на новую строчку
        return area;
    }
}