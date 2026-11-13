package pos.repo;
public class RepositorioException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public RepositorioException(String mensaje, Throwable causa) { super(mensaje, causa); }
}
