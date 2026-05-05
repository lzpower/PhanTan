package dao.impl;

import dao.GenericDao;
import db.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.function.Function;

public abstract class AbstractGenericDaoImpl<T, ID> implements GenericDao<T, ID> {

    protected final Class<T> entityClass;

    protected AbstractGenericDaoImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected <R> R doInTransaction(Function<EntityManager, R> function) {
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = JPAUtil.getEntityManager();
            tx = em.getTransaction();
            tx.begin();
            R result = function.apply(em);
            tx.commit();
            return result;
        } catch (Exception ex) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException(ex);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public T create(T t) {
        return doInTransaction(em -> {
            em.persist(t);
            return t;
        });
    }

    @Override
    public T update(T t) {
        return doInTransaction(em -> em.merge(t));
    }

    @Override
    public boolean delete(ID id) {
        return doInTransaction(em -> {
            T t = em.find(entityClass, id);
            if (t == null) {
                return false;
            }
            em.remove(t);
            return true;
        });
    }

    @Override
    public T findById(ID id) {
        return doInTransaction(em -> em.find(entityClass, id));
    }

    @Override
    public List<T> loadAll() {
        String query = "FROM " + entityClass.getSimpleName();
        return doInTransaction(em -> em.createQuery(query, entityClass).getResultList());
    }
}
