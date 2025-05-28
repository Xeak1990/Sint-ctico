package codigo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.StringReader;

public class Principal {
    public static void main(String[] args) {
        JFrame ventana = new JFrame("Analizador Léxico y Sintáctico - NeoJava");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(1000, 600);
        ventana.setLayout(new BorderLayout());

        // Área izquierda: entrada de código
        JTextArea areaCodigo = new JTextArea();
        JScrollPane scrollCodigo = new JScrollPane(areaCodigo);
        scrollCodigo.setBorder(BorderFactory.createTitledBorder("Código fuente"));

        // Área derecha: salida de tokens
        JTextArea areaTokens = new JTextArea();
        areaTokens.setEditable(false);
        JScrollPane scrollTokens = new JScrollPane(areaTokens);
        scrollTokens.setBorder(BorderFactory.createTitledBorder("Tokens léxicos"));

        // Área inferior: salida de análisis sintáctico
        JTextArea areaSintactico = new JTextArea(5, 80);
        areaSintactico.setEditable(false);
        areaSintactico.setForeground(Color.RED);
        JScrollPane scrollSintactico = new JScrollPane(areaSintactico);
        scrollSintactico.setBorder(BorderFactory.createTitledBorder("Resultado del análisis sintáctico"));

        // Botón de analizar
        JButton botonAnalizar = new JButton("Analizar");

        botonAnalizar.addActionListener((ActionEvent e) -> {
            String input = areaCodigo.getText();
            areaTokens.setText("");
            areaSintactico.setText("");
            Parser.errores.clear();

            try {
                // Análisis léxico
                Lexer lexer = new Lexer(new StringReader(input));
                int contador = 1;
                while (true) {
                    java_cup.runtime.Symbol token = lexer.next_token();
                    if (token.sym == sym.EOF) break;
                    String tokenName = sym.terminalNames[token.sym];
                    areaTokens.append(String.format("Token #%d: %-20s Texto: %s\n", contador++, tokenName, token.value != null ? token.value : ""));
                }

                // Análisis sintáctico
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
        });

        // Panel principal
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollCodigo, scrollTokens);
        splitPane.setResizeWeight(0.5);
        JPanel panelCentro = new JPanel(new BorderLayout());
        panelCentro.add(splitPane, BorderLayout.CENTER);
        panelCentro.add(scrollSintactico, BorderLayout.SOUTH);

        ventana.add(panelCentro, BorderLayout.CENTER);
        ventana.add(botonAnalizar, BorderLayout.SOUTH);
        ventana.setVisible(true);
    }
}
