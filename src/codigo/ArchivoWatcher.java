package codigo;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class ArchivoWatcher extends Thread {
    private final Path archivo;
    private final Runnable accion;

    public ArchivoWatcher(Path archivo, Runnable accionAlCambiar) {
        this.archivo = archivo;
        this.accion = accionAlCambiar;
    }

    @Override
    public void run() {
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            Path carpeta = archivo.getParent();
            carpeta.register(watcher, ENTRY_MODIFY);

            while (!isInterrupted()) {
                WatchKey key = watcher.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path modificado = (Path) event.context();
                    if (modificado.toString().equals(archivo.getFileName().toString())) {
                        accion.run();
                    }
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Watcher detenido o error: " + e.getMessage());
        }
    }
}
