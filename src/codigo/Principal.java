package codigo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.*;
import java.util.Objects;

public class Principal {
    private static Path archivoActual = Paths.get("Códigos_Fuentes/Codigo1.txt");
    private static ArchivoWatcher watcher;

    public static void main(String[] args) {
        JFrame ventana = new JFrame("Analizador Léxico y Sintáctico - NeoJava");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(1200, 700);
        ventana.setLayout(new BorderLayout());

        // Área izquierda: código fuente
        JTextArea areaCodigo = new JTextArea();
        areaCodigo.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollCodigo = new JScrollPane(areaCodigo);
        scrollCodigo.setBorder(BorderFactory.createTitledBorder("Código fuente"));

        // Área derecha: tabla de tokens
        String[] columnas = {"Token #", "Lexema", "Patrón"};
        DefaultTableModel modeloTokens = new DefaultTableModel(columnas, 0);
        JTable tablaTokens = new JTable(modeloTokens);
        JScrollPane scrollTokens = new JScrollPane(tablaTokens);
        scrollTokens.setPreferredSize(new Dimension(300, 200));
        scrollTokens.setBorder(BorderFactory.createTitledBorder("Análisis léxico"));

        // Área inferior: análisis sintáctico
        JTextArea areaSintactico = new JTextArea(5, 80);
        areaSintactico.setEditable(false);
        JScrollPane scrollSintactico = new JScrollPane(areaSintactico);
        scrollSintactico.setBorder(BorderFactory.createTitledBorder("Análisis sintáctico"));

        // Botones
        JButton botonAnalizar = new JButton("Analizar código");
        JButton botonAbrirArchivo = new JButton("Abrir archivo .txt");

        botonAnalizar.addActionListener((ActionEvent e) -> {
            analizarCodigo(areaCodigo.getText(), modeloTokens, areaSintactico);
        });

        botonAbrirArchivo.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser("Códigos_Fuentes");
            fileChooser.setDialogTitle("Seleccionar archivo .txt");
            int opcion = fileChooser.showOpenDialog(ventana);
            if (opcion == JFileChooser.APPROVE_OPTION) {
                archivoActual = fileChooser.getSelectedFile().toPath();
                cargarArchivo(areaCodigo);
                reiniciarWatcher(areaCodigo);
            }
        });

        // Layout central y botones
        JSplitPane splitCentro = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollCodigo, scrollTokens);
        splitCentro.setResizeWeight(0.7);
        JPanel panelCentro = new JPanel(new BorderLayout());
        panelCentro.add(splitCentro, BorderLayout.CENTER);
        panelCentro.add(scrollSintactico, BorderLayout.SOUTH);

        JPanel panelBotones = new JPanel();
        panelBotones.add(botonAbrirArchivo);
        panelBotones.add(botonAnalizar);

        ventana.add(panelCentro, BorderLayout.CENTER);
        ventana.add(panelBotones, BorderLayout.SOUTH);
        ventana.setVisible(true);

        // Cargar el archivo inicial
        cargarArchivo(areaCodigo);
        reiniciarWatcher(areaCodigo);
    }

    private static void cargarArchivo(JTextArea areaCodigo) {
        if (Files.exists(archivoActual)) {
            try {
                String contenido = Files.readString(archivoActual);
                areaCodigo.setText(contenido);
            } catch (IOException e) {
                areaCodigo.setText("// Error al leer el archivo.");
            }
        } else {
            areaCodigo.setText("// Archivo no encontrado.");
        }
    }

    private static void reiniciarWatcher(JTextArea areaCodigo) {
        if (watcher != null && watcher.isAlive()) {
            watcher.interrupt();
        }

        watcher = new ArchivoWatcher(archivoActual, () -> {
            try {
                String contenido = Files.readString(archivoActual);
                SwingUtilities.invokeLater(() -> areaCodigo.setText(contenido));
            } catch (IOException ex) {
                System.out.println("Error leyendo archivo actualizado: " + ex.getMessage());
            }
        });
        watcher.start();
    }

    private static void analizarCodigo(String input, DefaultTableModel modeloTokens, JTextArea areaSintactico) {
        modeloTokens.setRowCount(0);
        areaSintactico.setText("");
        Parser.errores.clear();

        try {
            Lexer lexer = new Lexer(new StringReader(input));
            int contador = 1;
            java_cup.runtime.Symbol token;

            while (!Objects.isNull(token = lexer.next_token()) && token.sym != sym.EOF) {
                String lexema = (token.value != null) ? token.value.toString() : "";
                String patron = sym.terminalNames[token.sym];
                modeloTokens.addRow(new Object[]{contador++, lexema, patron});
            }

            Lexer lexer2 = new Lexer(new StringReader(input));
            Parser parser = new Parser(lexer2);
            parser.parse();

            if (Parser.errores.isEmpty()) {
                areaSintactico.setForeground(Color.GREEN.darker());
                areaSintactico.setText("Análisis sintáctico realizado con éxito. No se encontraron errores.");
            } else {
                areaSintactico.setForeground(Color.RED.darker());
                areaSintactico.append("Se encontraron " + Parser.errores.size() + " errores sintácticos:\n");
                for (String error : Parser.errores) {
                    areaSintactico.append(" - " + error + "\n");
                }
            }
        } catch (Exception ex) {
            areaSintactico.setForeground(Color.RED);
            areaSintactico.setText("Error durante el análisis: " + ex.getMessage());
        }
    }
}
