package com.gld.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gld.dto.Saree;

public interface SareeRepository extends JpaRepository<Saree, Integer>{

	List<Saree> findBySareeTypeIgnoreCase(String sareeType);

	List<Saree> findBySareeTypeAndColorIgnoreCase(String sareeType, String color);

	List<String> findDistinctColorsBySareeType(String sareeType);

	List<Saree> findBySareeTypeIgnoreCaseAndColorIgnoreCase(String sareeType, String color);

	List<Saree> findBySareeTypeIgnoreCaseAndDesignIgnoreCase(String sareeType, String design);

	List<Saree> findBySareeTypeIgnoreCaseAndPriceBetween(String sareeType, Double minPrice, Double maxPrice);

	List<String> findDistinctDesignsBySareeType(String sareeType);

	List<Saree> findByFabric(String fabric);

	List<Saree> findByWeavingType(String weavingType);

	List<Saree> findByName(String name);

	List<Saree> findByDesign(String design);

	List<Saree> findBySareeType(String sareeType);

	List<Saree> findByPublishedTrue();
}
