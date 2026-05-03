package com.musicstreaming.repository;

import com.musicstreaming.model.Album;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Integer>, AlbumRepositoryCustom {

    @EntityGraph(attributePaths = {"artist"})
    List<Album> findByArtistIdOrderByReleaseDateDesc(Integer artistId);

    @EntityGraph(attributePaths = {"artist"})
    @Query("SELECT a FROM Album a ORDER BY a.releaseDate DESC NULLS LAST, a.title")
    List<Album> findAllOrdered();

    @EntityGraph(attributePaths = {"artist"})
    @Query("SELECT a FROM Album a ORDER BY a.releaseDate DESC NULLS LAST")
    List<Album> findNewReleases(Pageable pageable);

    @EntityGraph(attributePaths = {"artist"})
    @Override
    Optional<Album> findById(Integer id);
}

interface AlbumRepositoryCustom {
    List<Album> searchByCriteria(String query);
}

@Repository
class AlbumRepositoryImpl implements AlbumRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Album> searchByCriteria(String query) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Album> cq = cb.createQuery(Album.class);
        Root<Album> album = cq.from(Album.class);

        album.fetch("artist", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            String searchPattern = "%" + query.toLowerCase() + "%";

            // Поиск по названию альбома
            Predicate titlePredicate = cb.like(cb.lower(album.get("title")), searchPattern);

            // Поиск по имени исполнителя
            Join<Object, Object> artist = album.join("artist", JoinType.LEFT);
            Predicate artistPredicate = cb.like(cb.lower(artist.get("name")), searchPattern);

            predicates.add(cb.or(titlePredicate, artistPredicate));
        }

        cq.where(predicates.toArray(new Predicate[0]));

        // Сортировка
        List<Order> orders = new ArrayList<>();
        orders.add(cb.desc(album.get("releaseDate")));
        orders.add(cb.asc(album.get("title")));
        cq.orderBy(orders);

        return entityManager.createQuery(cq).getResultList();
    }
}