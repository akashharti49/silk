package com.gld.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gld.dto.Saree;
import com.gld.dto.Silk;
import com.gld.repository.SareeRepository;
import com.gld.repository.SilkRepository;
import com.gld.service.implentation.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {
	
	@Autowired
	UserService userService;
	
	@Autowired
	SareeRepository sareeRepository;
	
	@Autowired
	SilkRepository silkRepository;
	
	@GetMapping("/")
	public String loadHome(ModelMap map) {
		return userService.loadHome(map);
	}
	
	@GetMapping("/login")
	public String loadLogin() {
		return userService.loadLogin();
	}
	
	@PostMapping("/login")
	public String login(@RequestParam String email,@RequestParam String password,HttpSession session)
	{
		return userService.login(email,password,session);
	}
	
	@GetMapping("/logout")
	public String logout(HttpSession session)
	{
		return userService.logout(session);
	}
	
	@GetMapping("/sarees/type/{sareeType}")
	public String sareesByType(@PathVariable String sareeType, ModelMap map) {

	    List<Saree> sarees = sareeRepository.findBySareeTypeIgnoreCase(sareeType);

	    map.put("sarees", sarees);
	    map.put("sareeType", sareeType);

	    return "saree-type.html";
	}
	
	@GetMapping("/silks/type/{type}")
	public String loadSilkType(@PathVariable String type, ModelMap map) {

	    List<Silk> silks =
	            silkRepository.findBySilkTypeIgnoreCase(type);

	    map.put("silks", silks);
	    map.put("silkType", type);

	    return "silk-type.html";
	}
	
	
	@GetMapping("/sarees")
	public String viewSarees(
	        @RequestParam(required = false) String sareeType,
	        @RequestParam(required = false) String color,
	        @RequestParam(required = false) String design,
	        @RequestParam(required = false) String fabric,
	        @RequestParam(required = false) String weavingType,
	        @RequestParam(required = false) String name,
	        ModelMap map) {

	    List<Saree> sarees;

	    if (color != null && !color.isEmpty()) {

	        sarees = sareeRepository
	                .findBySareeTypeIgnoreCaseAndColorIgnoreCase(sareeType,color);

	    } else if (fabric != null && !fabric.isEmpty()) {

	        sarees = sareeRepository
	                .findByFabric(fabric);

	    } else if (weavingType != null && !weavingType.isEmpty()) {

	        sarees = sareeRepository
	                .findByWeavingType(weavingType);

	    } else if (name != null && !name.isEmpty()) {

	        sarees = sareeRepository
	                .findByName(name);

	    } else if (design != null && !design.isEmpty()) {

	        sarees = sareeRepository
	                .findByDesign(design);

	    } else if (sareeType != null && !sareeType.isEmpty()) {

	        sarees = sareeRepository
	                .findBySareeType(sareeType);

	    } else {

	        sarees = sareeRepository.findAll();
	    }

	    map.put("sarees", sarees);
	    map.put("sareeType", sareeType);

	    return "saree-type.html";
	}
	
	
	@GetMapping("/silks")
	public String viewSilks(

	        @RequestParam(required = false) String silkType,

	        @RequestParam(required = false) String color,

	        @RequestParam(required = false) String design,

	        @RequestParam(required = false) String fabric,

	        @RequestParam(required = false) String weavingType,

	        @RequestParam(required = false) String origin,

	        @RequestParam(required = false) String finish,

	        @RequestParam(required = false) String name,

	        ModelMap map) {


	    List<Silk> silks;


	    if (color != null && !color.isEmpty()) {

	        silks = silkRepository
	                .findBySilkTypeIgnoreCaseAndColorIgnoreCase(
	                        silkType,
	                        color
	                );

	    }

	    else if (fabric != null && !fabric.isEmpty()) {

	        silks = silkRepository
	                .findByFabric(fabric);

	    }

	    else if (weavingType != null && !weavingType.isEmpty()) {

	        silks = silkRepository
	                .findByWeavingType(weavingType);

	    }

	    else if (origin != null && !origin.isEmpty()) {

	        silks = silkRepository
	                .findByOrigin(origin);

	    }

	    else if (finish != null && !finish.isEmpty()) {

	        silks = silkRepository
	                .findByFinish(finish);

	    }

	    else if (name != null && !name.isEmpty()) {

	        silks = silkRepository
	                .findByName(name);

	    }

	    else if (design != null && !design.isEmpty()) {

	        silks = silkRepository
	                .findByDesign(design);

	    }

	    else if (silkType != null && !silkType.isEmpty()) {

	        silks = silkRepository
	                .findBySilkTypeIgnoreCase(silkType);

	    }

	    else {

	        silks = silkRepository.findAll();

	    }


	    map.put("silks", silks);

	    map.put("silkType", silkType);


	    return "silk-type.html";
	}
	
	@GetMapping("/sarees/{id}")
	public String viewSareeDetails(@PathVariable int id, Model model) {

	    Saree saree = userService.getSareeById(id);

	    model.addAttribute("saree", saree);

	    return "saree-details.html";
	}
	
	@GetMapping("/silks/{id}")
	public String viewSilkDetails(
	        @PathVariable int id,
	        Model model) {

	    Silk silk = userService.getSilkById(id);

	    model.addAttribute("silk", silk);

	    return "silk-details";
	}
	
	
	
	
	

	
	

}
