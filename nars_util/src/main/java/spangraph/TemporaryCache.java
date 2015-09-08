package spangraph;

import org.infinispan.commons.marshall.Marshaller;
import sun.security.action.GetPropertyAction;

import java.io.IOException;
import java.nio.file.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.security.AccessController.doPrivileged;

/**
 * Created by me on 9/8/15.
 */
public class TemporaryCache {

    private static Path dir;
    private static Path tmp = Paths.get(doPrivileged(new GetPropertyAction("java.io.tmpdir")));

    static {
        try {
            Files.createDirectory( dir = tmp.resolve("opennars") );
        }
        catch (FileAlreadyExistsException e) {
            //ok.
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static <T> T computeIfAbsent(String key, Marshaller m, Supplier<T> builder) {
        return computeIfAbsent( key, m, builder, (x) -> true);
    }

    public static synchronized <T> T computeIfAbsent(String key, Marshaller m, Supplier<T> builder, Predicate<T> validator) {

        Path f = dir.resolve(key);

        try {

            byte[] b = Files.readAllBytes(f);
            T t = (T) m.objectFromByteBuffer(b);
            if (validator.test(t)) {
                return t;
            }
        } catch (NoSuchFileException e) {
            //ok.
        } catch (Exception e) {
            e.printStackTrace();
        }


        T t = builder.get();
        if (!validator.test(t)) {
            throw new RuntimeException("validator false for: " + t);
        }

        try {
            Files.write(f, m.objectToByteBuffer(t));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return t;
    }
}
