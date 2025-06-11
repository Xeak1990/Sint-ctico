package codigo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Principal {
    private static Path archivoActual = Paths.get("Códigos_Fuentes/Codigo1.txt"); // Ruta por defecto
    private static ArchivoWatcher watcher;

    // Componentes de la UI para poder actualizarlos
    private static JTextArea areaCodigo;
    private static DefaultTableModel modeloTokens;
    private static JTextArea areaSintactico;
    private static JTextArea areaErrores;

    // Listas para almacenar errores léxicos y sintácticos
    public static List<AnalizadorError> erroresLexicos = new ArrayList<>();
    public static List<AnalizadorError> erroresSintacticos = new ArrayList<>();

    public static void main(String[] args) {
        JFrame ventana = new JFrame("Analizador Léxico y Sintáctico - NeoJava");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(1200, 800);
        ventana.setLayout(new BorderLayout());

        // Área izquierda: código fuente
        areaCodigo = new JTextArea(); // Inicialización aquí
        areaCodigo.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollCodigo = new JScrollPane(areaCodigo);
        scrollCodigo.setBorder(BorderFactory.createTitledBorder("Código fuente"));

        // Área derecha: tabla de tokens
        String[] columnas = {"Token #", "Lexema", "Patrón"};
        modeloTokens = new DefaultTableModel(columnas, 0); // Inicialización aquí
        JTable tablaTokens = new JTable(modeloTokens);
        JScrollPane scrollTokens = new JScrollPane(tablaTokens);
        scrollTokens.setPreferredSize(new Dimension(300, 200));
        scrollTokens.setBorder(BorderFactory.createTitledBorder("Análisis léxico"));

        // Área inferior central: resultado sintáctico (si no hay errores)
        areaSintactico = new JTextArea(); // Inicialización aquí
        areaSintactico.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaSintactico.setEditable(false);
        JScrollPane scrollSintactico = new JScrollPane(areaSintactico);
        scrollSintactico.setPreferredSize(new Dimension(800, 80));
        scrollSintactico.setBorder(BorderFactory.createTitledBorder("Resultado Análisis Sintáctico"));

        // Área de Errores General
        areaErrores = new JTextArea(); // Inicialización aquí
        areaErrores.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaErrores.setEditable(false);
        areaErrores.setForeground(Color.RED);
        JScrollPane scrollErrores = new JScrollPane(areaErrores);
        scrollErrores.setPreferredSize(new Dimension(800, 200));
        scrollErrores.setBorder(BorderFactory.createTitledBorder("Errores Detectados"));

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton btnAbrirArchivo = new JButton("Abrir Archivo"); // Botón para abrir archivo
        JButton btnAnalizarLexico = new JButton("Analizar Léxicamente");
        JButton btnAnalizarSintactico = new JButton("Analizar Sintácticamente");
        JButton btnLimpiar = new JButton("Limpiar Tablas y Errores");

        panelBotones.add(btnAbrirArchivo);
        panelBotones.add(btnAnalizarLexico);
        panelBotones.add(btnAnalizarSintactico);
        panelBotones.add(btnLimpiar);

        // Agregando paneles a la ventana
        JPanel panelSuperior = new JPanel(new GridLayout(1, 2));
        panelSuperior.add(scrollCodigo);
        panelSuperior.add(scrollTokens);

        JPanel panelInferiorContenedor = new JPanel();
        panelInferiorContenedor.setLayout(new BoxLayout(panelInferiorContenedor, BoxLayout.Y_AXIS));
        panelInferiorContenedor.add(scrollSintactico);
        panelInferiorContenedor.add(scrollErrores);

        ventana.add(panelBotones, BorderLayout.NORTH);
        ventana.add(panelSuperior, BorderLayout.CENTER);
        ventana.add(panelInferiorContenedor, BorderLayout.SOUTH);

        ventana.setVisible(true);

        // Cargar el archivo inicial
        cargarArchivoInicial();

        // Configurar el watcher de archivo
        configurarWatcher();

        // Action Listeners para los botones
        btnAbrirArchivo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirArchivo();
            }
        });

        btnAnalizarLexico.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                analizarLexicamente(areaCodigo.getText(), modeloTokens, areaErrores);
                areaSintactico.setText(""); // Limpiar resultado sintáctico al hacer solo léxico
            }
        });

        btnAnalizarSintactico.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Primero se debe realizar un análisis léxico para tener los tokens
                // Re-ejecutar léxico para asegurar que los tokens sean actuales
                analizarLexicamente(areaCodigo.getText(), modeloTokens, areaErrores);
                analizarSintacticamente(areaCodigo.getText(), areaSintactico, areaErrores);
            }
        });

        btnLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiar();
            }
        });

        // Asegurarse de detener el watcher al cerrar la aplicación
        ventana.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (watcher != null) {
                    watcher.interrupt(); // Detener el hilo del watcher
                }
            }
        });
    }

    /**
     * Carga el contenido del archivo inicial en el área de código.
     */
    private static void cargarArchivoInicial() {
        try {
            areaCodigo.setText(new String(Files.readAllBytes(archivoActual)));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al cargar el archivo inicial: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            areaCodigo.setText("// Error al cargar el archivo inicial: " + e.getMessage());
        }
    }

    /**
     * Configura el ArchivoWatcher para el archivo actual.
     * Detiene el watcher anterior si existe y lo inicia para el nuevo archivo.
     */
    private static void configurarWatcher() {
        if (watcher != null) {
            watcher.interrupt(); // Detener el watcher anterior si está corriendo
            try {
                watcher.join(); // Esperar a que el hilo termine
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
            }
        }
        watcher = new ArchivoWatcher(archivoActual, () -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    // Al cambiar el archivo, solo actualiza el código y limpia resultados previos.
                    // El análisis se disparará con los botones.
                    areaCodigo.setText(new String(Files.readAllBytes(archivoActual)));
                    limpiar(); // Limpiar al recargar el archivo
                    JOptionPane.showMessageDialog(null, "Archivo " + archivoActual.getFileName() + " ha sido modificado y recargado.", "Archivo Modificado", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error al leer el archivo modificado: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        });
        watcher.start();
    }

    /**
     * Abre un cuadro de diálogo para que el usuario seleccione un archivo.
     * Actualiza el archivo actual y recarga su contenido.
     */
    private static void abrirArchivo() {
        JFileChooser fileChooser = new JFileChooser(archivoActual.getParent().toFile());
        int resultado = fileChooser.showOpenDialog(null);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                archivoActual = selectedFile.toPath();
                try {
                    areaCodigo.setText(new String(Files.readAllBytes(archivoActual)));
                    limpiar(); // Limpiar resultados al cargar un nuevo archivo
                    configurarWatcher(); // Reconfigurar el watcher para el nuevo archivo
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error al leer el archivo seleccionado: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Realiza el análisis léxico del código fuente.
     *
     * @param input El código fuente a analizar.
     * @param modeloTokens El modelo de la tabla para mostrar los tokens.
     * @param areaErrores El área de texto para mostrar los errores.
     */
    private static void analizarLexicamente(String input, DefaultTableModel modeloTokens, JTextArea areaErrores) {
        modeloTokens.setRowCount(0); // Limpiar tabla de tokens
        erroresLexicos.clear();      // Limpiar errores léxicos previos
        erroresSintacticos.clear();  // Limpiar errores sintácticos (si los hubiera)
        Parser.errores.clear();      // Limpiar la lista de errores del parser (si los hubiera)
        areaErrores.setText("");     // Limpiar área de errores para este análisis

        try {
            Lexer lexer = new Lexer(new StringReader(input));
            int contador = 1;
            java_cup.runtime.Symbol token;

            while (!Objects.isNull(token = lexer.next_token()) && token.sym != sym.EOF) {
                String lexema = (token.value != null) ? token.value.toString() : "";
                String patron = sym.terminalNames[token.sym];
                modeloTokens.addRow(new Object[]{contador++, lexema, patron});

                // Si el lexer devuelve un token de error, regístralo
                if (token.sym == sym.error) {
                    // La línea y columna se obtienen de los atributos 'left' y 'right' del Symbol
                    erroresLexicos.add(new AnalizadorError(
                        AnalizadorError.TipoError.LEXICO,
                        "Carácter o secuencia inválida: '" + lexema + "'",
                        token.left + 1, // yyline es 0-indexed en JFlex
                        token.right + 1 // yycolumn es 0-indexed en JFlex
                    ));
                }
            }
        } catch (IOException ex) {
            erroresLexicos.add(new AnalizadorError(
                AnalizadorError.TipoError.LEXICO,
                "Error de lectura durante el análisis léxico: " + ex.getMessage(),
                0, 0
            ));
        } catch (Exception ex) { // Capturar cualquier otra excepción de JFlex
            erroresLexicos.add(new AnalizadorError(
                AnalizadorError.TipoError.LEXICO,
                "Error inesperado en el analizador léxico: " + ex.getMessage(),
                0, 0
            ));
        } finally {
            // Mostrar todos los errores encontrados hasta ahora
            mostrarErrores(areaErrores);
        }
    }

    /**
     * Realiza el análisis sintáctico del código fuente.
     * Asume que un análisis léxico ya ha sido realizado para obtener los tokens.
     *
     * @param input El código fuente a analizar.
     * @param areaSintactico El área de texto para mostrar el resultado sintáctico.
     * @param areaErrores El área de texto para mostrar los errores.
     */
    private static void analizarSintacticamente(String input, JTextArea areaSintactico, JTextArea areaErrores) {
        areaSintactico.setText("");      // Limpiar área de resultado sintáctico
        erroresSintacticos.clear();      // Limpiar errores sintácticos previos
        Parser.errores.clear();          // Asegurarse de limpiar la lista de errores del Parser

        try {
            Lexer lexer = new Lexer(new StringReader(input));
            Parser parser = new Parser(lexer);
            parser.parse(); // Intenta parsear

            if (Parser.errores.isEmpty()) { // Si CUP no reportó errores en su lista
                areaSintactico.setForeground(new Color(0, 150, 0)); // Un verde oscuro
                areaSintactico.setText("Análisis sintáctico realizado con éxito. No se encontraron errores sintácticos.");
            } else {
                areaSintactico.setForeground(Color.RED.darker());
                areaSintactico.setText("Se encontraron errores sintácticos. Ver la sección 'Errores Detectados'.");

                for (String errorMsg : Parser.errores) {
                    // Aquí asumimos que el errorMsg de Parser ya contiene la información de línea/columna
                    // Si tu parser.jalr está configurado para darla, genial.
                    // Si no, necesitarías parsear el string o, mejor, modificar el .cup para que añada AnalizadorError.
                    // Por simplicidad, si no podemos extraer línea/columna, ponemos 0.
                    int linea = 0;
                    int columna = 0;
                    String mensajeLimpio = errorMsg;

                    // Intento rudimentario de extraer línea y columna si el formato es "Error...en línea X, columna Y"
                    // Esto es idealmente manejado por la modificación del .cup
                    try {
                        int indexLinea = errorMsg.indexOf("línea ");
                        int indexColumna = errorMsg.indexOf("columna ");
                        if (indexLinea != -1 && indexColumna != -1) {
                            String sub = errorMsg.substring(indexLinea + 6);
                            int endLinea = sub.indexOf(",");
                            if (endLinea != -1) {
                                linea = Integer.parseInt(sub.substring(0, endLinea).trim());
                                sub = sub.substring(endLinea + 1);
                                int endCol = sub.indexOf(")");
                                if (endCol != -1) {
                                    columna = Integer.parseInt(sub.substring(sub.indexOf("columna ") + 8, endCol).trim());
                                }
                            }
                            mensajeLimpio = errorMsg.substring(0, indexLinea).trim(); // Mensaje sin la parte de línea/columna
                        }
                    } catch (NumberFormatException ex) {
                        // Si falla el parseo, se quedan en 0.
                    }


                    erroresSintacticos.add(new AnalizadorError(
                        AnalizadorError.TipoError.SINTACTICO,
                        mensajeLimpio, // Mensaje del error
                        linea,         // Línea
                        columna        // Columna
                    ));
                }
            }
        } catch (Exception ex) {
            areaSintactico.setForeground(Color.RED);
            areaSintactico.setText("Error fatal durante el análisis sintáctico: " + ex.getMessage());
            // Si hay una excepción general, es un error sintáctico grave
            erroresSintacticos.add(new AnalizadorError(
                AnalizadorError.TipoError.SINTACTICO,
                "Error fatal irrecuperable: " + ex.getMessage(),
                0, 0 // No se puede determinar línea/columna aquí en un error fatal
            ));
        } finally {
            // Mostrar todos los errores (léxicos y sintácticos combinados)
            mostrarErrores(areaErrores);
        }
    }

    /**
     * Limpia la tabla de tokens, el área de resultados sintácticos y el área de errores.
     */
    private static void limpiar() {
        modeloTokens.setRowCount(0);
        areaSintactico.setText("");
        areaErrores.setText("");
        erroresLexicos.clear();
        erroresSintacticos.clear();
        Parser.errores.clear(); // Limpiar también la lista de errores del parser
    }

    /**
     * Muestra todos los errores léxicos y sintácticos en el área de errores.
     *
     * @param areaErrores El JTextArea donde se mostrarán los errores.
     */
    private static void mostrarErrores(JTextArea areaErrores) {
        areaErrores.setText(""); // Limpiar antes de mostrar

        if (erroresLexicos.isEmpty() && erroresSintacticos.isEmpty()) {
            areaErrores.setForeground(Color.BLUE);
            areaErrores.setText("¡Análisis completado! No se encontraron errores léxicos ni sintácticos.");
            return;
        }

        areaErrores.setForeground(Color.RED);
        areaErrores.append("--- Errores Detectados ---\n\n");

        if (!erroresLexicos.isEmpty()) {
            areaErrores.append("=== Errores Léxicos ===\n");
            for (AnalizadorError error : erroresLexicos) {
                areaErrores.append(error.toString() + "\n");
            }
            areaErrores.append("\n");
        } else {
            areaErrores.append("No se encontraron errores léxicos.\n\n");
        }

        if (!erroresSintacticos.isEmpty()) {
            areaErrores.append("=== Errores Sintácticos ===\n");
            for (AnalizadorError error : erroresSintacticos) {
                areaErrores.append(error.toString() + "\n");
            }
        } else {
            areaErrores.append("No se encontraron errores sintácticos.\n");
        }
    }
}