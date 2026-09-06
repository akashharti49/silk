package com.gld.service.implentation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import com.gld.dto.Saree;
import com.gld.dto.Silk;
import com.gld.repository.SareeRepository;
import com.gld.repository.SilkRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class UserServiceImpl implements UserService{
	
	@Autowired
	SareeRepository sareeRepository;
	
	@Autowired
	SilkRepository silkRepository;
	
	@Value("${admin.email}")
	private String adminEmail;

	@Value("${admin.password}")
	private String adminPassword;
	
	public String loadHome(ModelMap map) {

	    List<Saree> sarees = sareeRepository.findByPublishedTrue();
	    map.put("sarees", sarees);
	    
	    List<Silk> silk = silkRepository.findByPublishedTrue();

	    map.put("silk", silk);

	    

	    return "home.html";
	}

	@Override
	public String loadLogin() {
		return "Login.html";
	}

	@Override
	public String login(String email, String password, HttpSession session) {
		if (adminEmail.equals(email) && adminPassword.equals(password)) {

	        session.setAttribute("admin", "admin");
	        session.setAttribute("success", "Login Success");

	        return "redirect:/admin/home";

	    } else {

	        session.setAttribute("failure", "Invalid username or password");

	        return "Login.html";
	    }
	}

	@Override
	public String logout(HttpSession session) {
		session.removeAttribute("admin");
		session.setAttribute("success", "Logged out Success");
		return "redirect:/";	
		
	}

	@Override
	public Saree getSareeById(int id) {
		return sareeRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Saree not found with id: " + id));
	}

	@Override
	public Silk getSilkById(int id) {
		 return silkRepository.findById(id)
		            .orElseThrow(() ->
		                new RuntimeException(
		                    "Silk not found with id: " + id
		                )
		            );
	}
	
	
	
	

}
