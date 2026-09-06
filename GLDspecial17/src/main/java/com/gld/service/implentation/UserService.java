package com.gld.service.implentation;

import org.springframework.ui.ModelMap;

import com.gld.dto.Saree;
import com.gld.dto.Silk;

import jakarta.servlet.http.HttpSession;

public interface UserService {
	
	String loadHome(ModelMap map);

	String loadLogin();

	String login(String email, String password, HttpSession session);

	String logout(HttpSession session);

	Saree getSareeById(int id);

	Silk getSilkById(int id);

}
