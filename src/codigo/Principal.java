package codigo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.IOException;

public class Principal {
    public static void main(String[] args) {
        System.out.println("Escribe el texto a analizar. Escribe 'exit' para salir.");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            StringBuilder texto = new StringBuilder();
            String linea;

            while (true) {
                linea = br.readLine();
                if (linea == null || linea.equalsIgnoreCase("exit")) break;
                texto.append(linea).append("\n");
            }

            String input = texto.toString();

            // Primero: análisis léxico, solo para imprimir tokens
            Lexer lexer = new Lexer(new StringReader(input));
            System.out.println("Tokens léxicos:");
            while (true) {
                java_cup.runtime.Symbol token = lexer.next_token();
                if (token.sym == sym.EOF) break;

                // Puedes imprimir el token, su nombre y texto
                String tokenName = sym.terminalNames[token.sym];
                System.out.printf("Token: %-20s Texto: %s\n", tokenName, token.value != null ? token.value : "");
            }

            // Segundo: análisis sintáctico, nuevo lexer para que no esté vacío
            Lexer lexer2 = new Lexer(new StringReader(input));
            Parser sintactico = new Parser(lexer2);
            sintactico.parse();

            System.out.println("Análisis sintáctico realizado con éxito.");

        } catch (IOException e) {
            System.err.println("Error al leer desde la terminal: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error durante el análisis sintáctico: " + e.getMessage());
        }
    }
}
