package codigo;

public class AnalizadorError {
    public enum TipoError {
        LEXICO,
        SINTACTICO
    }

    private final TipoError tipo;
    private final String mensaje;
    private final int linea;
    private final int columna;

    public AnalizadorError(TipoError tipo, String mensaje, int linea, int columna) {
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.linea = linea;
        this.columna = columna;
    }

    public TipoError getTipo() {
        return tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    @Override
    public String toString() {
        // Formato para mostrar en el área de errores
        return String.format("%s: %s (Línea: %d, Columna: %d)", tipo.name(), mensaje, linea, columna);
    }
}