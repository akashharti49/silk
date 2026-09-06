package com.gld.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gld.dto.Silk;


public interface SilkRepository extends JpaRepository<Silk, Integer>{

	List<Silk> findByPublishedTrue();

	List<Silk> findBySilkTypeIgnoreCase(String type);

	List<Silk> findBySilkTypeIgnoreCaseAndColorIgnoreCase(String silkType, String color);

	List<Silk> findByFabric(String fabric);

	List<Silk> findByWeavingType(String weavingType);

	List<Silk> findByOrigin(String origin);

	List<Silk> findByFinish(String finish);

	List<Silk> findByName(String name);

	List<Silk> findByDesign(String design);

	List<Silk> findBySilkType(String silkType);

	List<Silk> findByColor(String color);

}
