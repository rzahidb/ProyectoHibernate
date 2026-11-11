
package org.example.proyectohibernate.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import org.example.proyectohibernate.dao.CategoriaDAO;
import org.example.proyectohibernate.dao.LibroDAO;
import org.example.proyectohibernate.model.Categoria;
import org.example.proyectohibernate.model.Libro;

import java.math.BigDecimal;

public class LibroController {

    @FXML private TextField txtTitulo;
    @FXML private TextField txtAutor;
    @FXML private TextField txtAnio;
    @FXML private TextField txtPrecio;

    @FXML private ComboBox<Categoria> comboCategoria;

    @FXML private TableView<Libro> tablaLibros;

    @FXML private TableColumn<Libro, Long> colId;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, String> colAutor;
    @FXML private TableColumn<Libro, Integer> colAnio;
    @FXML private TableColumn<Libro, BigDecimal> colPrecio;
    @FXML private TableColumn<Libro, Categoria> colCategoria;

    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;

    private final LibroDAO libroDAO = new LibroDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    private final ObservableList<Categoria> categorias =
            FXCollections.observableArrayList();

    private Long idSeleccionado;

    @FXML
    private void initialize() {

        colId.setCellValueFactory(
                new PropertyValueFactory<>("id"));

        colTitulo.setCellValueFactory(
                new PropertyValueFactory<>("titulo"));

        colAutor.setCellValueFactory(
                new PropertyValueFactory<>("autor"));

        colAnio.setCellValueFactory(
                new PropertyValueFactory<>("anioPublicacion"));

        colPrecio.setCellValueFactory(
                new PropertyValueFactory<>("precio"));

        colCategoria.setCellValueFactory(
                new PropertyValueFactory<>("categoria"));

        comboCategoria.setItems(categorias);

        configurarColumnaCategoria();

        tablaLibros.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, anterior, actual) -> {
                    if (actual != null) {
                        seleccionarLibro(actual);
                    } else {
                        idSeleccionado = null;
                        actualizarBotones();
                    }
                });

        actualizarBotones();
        cargarCategorias();
        cargarLibros();
    }

    // Configurar el ComboBox de la columna Categoría
    private void configurarColumnaCategoria() {

        tablaLibros.setEditable(true);
        colCategoria.setEditable(true);

        StringConverter<Categoria> conversor =
                new StringConverter<>() {

                    @Override
                    public String toString(Categoria categoria) {
                        return categoria == null
                                ? ""
                                : categoria.getNombre();
                    }

                    @Override
                    public Categoria fromString(String nombre) {
                        return categorias.stream()
                                .filter(c -> c.getNombre().equals(nombre))
                                .findFirst()
                                .orElse(null);
                    }
                };

        colCategoria.setCellFactory(
                ComboBoxTableCell.forTableColumn(
                        conversor, categorias
                )
        );

        colCategoria.setOnEditCommit(event -> {

            Libro libro = event.getRowValue();
            Categoria nueva = event.getNewValue();

            if (nueva == null) {
                tablaLibros.refresh();
                return;
            }

            Categoria anterior = libro.getCategoria();

            if (anterior != null &&
                    anterior.getId().equals(nueva.getId())) {
                return;
            }

            libro.setCategoria(nueva);

            try {
                libroDAO.actualizar(libro);

                if (libro.getId().equals(idSeleccionado)) {
                    comboCategoria.setValue(nueva);
                }

                tablaLibros.refresh();

            } catch (Exception ex) {
                libro.setCategoria(anterior);
                tablaLibros.refresh();

                mostrarAlerta(Alert.AlertType.ERROR,
                        "Error al cambiar categoría: "
                                + ex.getMessage());
            }
        });
    }

    // Consultar categorías en PostgreSQL
    @FXML
    private void cargarCategorias() {

        Categoria anterior = comboCategoria.getValue();

        try {
            categorias.setAll(categoriaDAO.listar());

            comboCategoria.getSelectionModel().clearSelection();

            if (anterior != null) {
                for (Categoria categoria : categorias) {
                    if (categoria.getId().equals(anterior.getId())) {
                        comboCategoria.setValue(categoria);
                        break;
                    }
                }
            }

        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "Error al cargar categorías: "
                            + ex.getMessage());
        }
    }

    // Guardar nuevo libro
    @FXML
    private void guardarLibro() {

        Libro libro = obtenerFormulario();
        if (libro == null) return;

        try {
            libroDAO.guardar(libro);
            cargarLibros();
            limpiarCampos();

            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Libro guardado correctamente.");

        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "Error al guardar: " + ex.getMessage());
        }
    }

    // Consultar libros
    @FXML
    private void cargarLibros() {

        try {
            tablaLibros.setItems(
                    FXCollections.observableArrayList(
                            libroDAO.listar()
                    )
            );

        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "Error al cargar libros: " + ex.getMessage());
        }
    }

    // Modificar libro
    @FXML
    private void modificarLibro() {

        if (idSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Seleccione un libro para modificar.");
            return;
        }

        Libro libro = obtenerFormulario();
        if (libro == null) return;

        libro.setId(idSeleccionado);

        try {
            libroDAO.actualizar(libro);
            cargarLibros();
            limpiarCampos();

            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Libro modificado correctamente.");

        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "Error al modificar: " + ex.getMessage());
        }
    }

    // Eliminar libro
    @FXML
    private void eliminarLibro() {

        if (idSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Seleccione un libro para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Está seguro de eliminar este libro?",
                ButtonType.YES,
                ButtonType.NO
        );

        if (confirmacion.showAndWait()
                .orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        try {
            boolean eliminado = libroDAO.eliminar(idSeleccionado);

            if (eliminado) {
                mostrarAlerta(Alert.AlertType.INFORMATION,
                        "Libro eliminado correctamente.");
            } else {
                mostrarAlerta(Alert.AlertType.WARNING,
                        "El libro no existe.");
            }

            cargarLibros();
            limpiarCampos();

        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "Error al eliminar: " + ex.getMessage());
        }
    }

    // Seleccionar libro de la tabla
    private void seleccionarLibro(Libro libro) {

        idSeleccionado = libro.getId();

        txtTitulo.setText(libro.getTitulo());
        txtAutor.setText(libro.getAutor());

        txtAnio.setText(
                String.valueOf(libro.getAnioPublicacion()));

        txtPrecio.setText(
                String.valueOf(libro.getPrecio()));

        comboCategoria.getSelectionModel().clearSelection();

        if (libro.getCategoria() != null) {
            for (Categoria categoria : categorias) {
                if (categoria.getId().equals(
                        libro.getCategoria().getId())) {
                    comboCategoria.setValue(categoria);
                    break;
                }
            }
        }

        actualizarBotones();
    }

    // Limpiar campos
    @FXML
    private void limpiarCampos() {

        txtTitulo.clear();
        txtAutor.clear();
        txtAnio.clear();
        txtPrecio.clear();

        comboCategoria.getSelectionModel().clearSelection();
        tablaLibros.getSelectionModel().clearSelection();

        idSeleccionado = null;
        actualizarBotones();
    }

    private void actualizarBotones() {

        boolean sinSeleccion = idSeleccionado == null;

        btnModificar.setDisable(sinSeleccion);
        btnEliminar.setDisable(sinSeleccion);
    }

    // Validar formulario
    private Libro obtenerFormulario() {

        String titulo = txtTitulo.getText().trim();
        String autor = txtAutor.getText().trim();
        Categoria categoria = comboCategoria.getValue();

        if (titulo.isEmpty() || autor.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Título y autor son obligatorios.");
            return null;
        }

        if (titulo.length() > 150 || autor.length() > 100) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Título máximo 150 y autor máximo 100 caracteres.");
            return null;
        }

        if (categoria == null) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Seleccione una categoría.");
            return null;
        }

        int anio;

        try {
            anio = Integer.parseInt(txtAnio.getText().trim());
        } catch (NumberFormatException ex) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Ingrese un año válido.");
            return null;
        }

        if (anio < 1450 || anio > 2100) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "El año debe estar entre 1450 y 2100.");
            return null;
        }

        BigDecimal precio;

        try {
            precio = new BigDecimal(txtPrecio.getText().trim());
        } catch (NumberFormatException ex) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Ingrese un precio válido.");
            return null;
        }

        if (precio.signum() < 0 ||
                precio.stripTrailingZeros().scale() > 2 ||
                precio.compareTo(
                        new BigDecimal("9999999999.99")) > 0) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "El precio debe ser no negativo, " +
                            "con máximo 2 decimales.");
            return null;
        }

        Libro libro = new Libro();

        libro.setTitulo(titulo);
        libro.setAutor(autor);
        libro.setAnioPublicacion(anio);
        libro.setPrecio(precio);
        libro.setCategoria(categoria);

        return libro;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {

        Alert alerta = new Alert(tipo, mensaje);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
