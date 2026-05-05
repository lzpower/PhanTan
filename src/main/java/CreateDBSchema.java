import jakarta.persistence.Persistence;

public class CreateDBSchema {

    public static void main(String[] args) {
        Persistence.createEntityManagerFactory("cua-hang-tien-loi-pu").createEntityManager();
    }
}
