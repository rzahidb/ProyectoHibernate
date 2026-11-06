package org.example.proyectohibernate.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import org.example.proyectohibernate.dao.LibroDAO;
import org.example.proyectohibernate.model.Libro;

import java.math.BigDecimal;

public class LibroController {

    @FXML private TextField txtTitulo;
    @FXML private TextField txtAutor;
    @FXML private TextField txtAnio;
    @FXML private TextField txtPrecio;

    @FXML private TableView<Libro> tablaLibros;

    @FXML private TableColumn<Libro, Long> colId;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, String> colAutor;
    @FXML private TableColumn<Libro, Integer> colAnio;
    @FXML private TableColumn<Libro, BigDecimal> colPrecio;

    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;

    private final LibroDAO libroDAO = new LibroDAO();
    private Long idSeleccionado = null;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anioPublicacion"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

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
    }

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

    @FXML
    private void cargarLibros() {
        try {
            tablaLibros.setItems(
                    FXCollections.observableArrayList(libroDAO.listar())
            );
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "Error al cargar libros: " + ex.getMessage());
        }
    }

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

        if (confirmacion.showAndWait().orElse(ButtonType.NO)
                != ButtonType.YES) {
            return;
        }

        try {
            boolean eliminado = libroDAO.eliminar(idSeleccionado);

            if (!eliminado) {
                mostrarAlerta(Alert.AlertType.WARNING,
                        "El libro no existe en la base de datos.");
                cargarLibros();
                limpiarCampos();
                return;
            }

            cargarLibros();
            limpiarCampos();
            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Libro eliminado correctamente.");

        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "Error al eliminar: " + ex.getMessage());
        }
    }

    @FXML
    private void limpiarCampos() {
        txtTitulo.clear();
        txtAutor.clear();
        txtAnio.clear();
        txtPrecio.clear();

        tablaLibros.getSelectionModel().clearSelection();
        idSeleccionado = null;
        actualizarBotones();
    }

    private void seleccionarLibro(Libro libro) {
        idSeleccionado = libro.getId();

        txtTitulo.setText(libro.getTitulo());
        txtAutor.setText(libro.getAutor());
        txtAnio.setText(String.valueOf(libro.getAnioPublicacion()));
        txtPrecio.setText(String.valueOf(libro.getPrecio()));

        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean sinSeleccion = idSeleccionado == null;
        btnModificar.setDisable(sinSeleccion);
        btnEliminar.setDisable(sinSeleccion);
    }

    private Libro obtenerFormulario() {
        String titulo = txtTitulo.getText().trim();
        String autor = txtAutor.getText().trim();

        if (titulo.isEmpty() || autor.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "El título y autor son obligatorios.");
            return null;
        }

        if (titulo.length() > 150 || autor.length() > 100) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "El título permite 150 caracteres y el autor 100.");
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
                precio.compareTo(new BigDecimal("9999999999.99")) > 0) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Ingrese un precio no negativo con máximo 2 decimales.");
            return null;
        }

        Libro libro = new Libro();
        libro.setTitulo(titulo);
        libro.setAutor(autor);
        libro.setAnioPublicacion(anio);
        libro.setPrecio(precio);

        return libro;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}