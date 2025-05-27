package codigo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.IOException;

public class Principal {
    public static void main(String[] args) {
        System.out.println("Escribe el texto a analizar. Escribe 'terminar' para salir.");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            StringBuilder texto = new StringBuilder();
            String linea;

            while (true) {
                linea = br.readLine();
                if (linea == null || linea.equalsIgnoreCase("terminar")) break;
                texto.append(linea).append("\n");
            }

            String input = texto.toString();

            Lexer lexer = new Lexer(new StringReader(input));
            System.out.println("Tokens léxicos:");

            int contador = 1;

            while (true) {
                java_cup.runtime.Symbol token = lexer.next_token();
                if (token.sym == sym.EOF) break;

                String tokenName = sym.terminalNames[token.sym];
                System.out.printf("Token #%d: %-20s Texto: %s\n", contador++, tokenName, token.value != null ? token.value : "");
            }

            Lexer lexer2 = new Lexer(new StringReader(input));
            Parser sintactico = new Parser(lexer2);
            sintactico.parse();

            // Mostrar errores sintácticos si existen
            if (!Parser.errores.isEmpty()) {
                System.err.println("\nSe encontraron " + Parser.errores.size() + " errores sintácticos:");
                for (String error : Parser.errores) {
                    System.err.println(" - " + error);
                }
            } else {
                System.out.println("Análisis sintáctico realizado con éxito. No se encontraron errores.");
            }

        } catch (IOException e) {
            System.err.println("Error al leer desde la terminal: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error durante el análisis sintáctico: " + e.getMessage());
        }
    }
}
