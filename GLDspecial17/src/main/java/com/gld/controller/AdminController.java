package com.gld.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.gld.dto.Saree;
import com.gld.dto.Silk;
import com.gld.service.implentation.AdminService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	AdminService adminService;
	
	
	@GetMapping("/home")
	public String loadHome(HttpSession session)
	{
		if(session.getAttribute("admin")!=null)
			return "admin-home.html";
		else {
			session.setAttribute("failure", "Invalid Session,Login Again");
			return "redirect:/login";
		}
	}
	
	@GetMapping("/add-saree")
	public String addMobileCover(HttpSession session,ModelMap map) {
		return adminService.addSaree(session,map);
	}
	
	@PostMapping("/add-saree")
	public String addSaree(HttpSession session,@Valid Saree saree,BindingResult result,@RequestParam("images") MultipartFile[] images) {

	    return adminService.addSaree(session, saree, result, images);
	}
	
	@GetMapping("/manage-saree")
	public String viewSaree(HttpSession session,ModelMap map) {
		return adminService.viewSaree(session,map);
	}
	
	@GetMapping("/delete-saree/{id}")
	public String deleteSaree(HttpSession session,@PathVariable int id) {
		return adminService.deleteSaree(session,id);
	}
	
	@GetMapping("/edit-saree/{id}")
	public String editSaree(HttpSession session,@PathVariable int id,ModelMap map) {
		return adminService.editSaree(session,id,map);
	}
	
	@PostMapping("/edit-saree")
	public String updateSaree(HttpSession session,@Valid Saree saree,BindingResult result,List<MultipartFile> images,@RequestParam(required = false) List<String> removeImages) {
	    return adminService.updateSaree(session,saree,result,images,removeImages);
	}
	@GetMapping("/add-silk")
	public String addSilk(HttpSession session,ModelMap map) {
		return adminService.addSilk(session,map);
	}
	
	@PostMapping("/add-silk")
	public String addSilk(HttpSession session,@Valid Silk silk,BindingResult result,@RequestParam("images") MultipartFile[] images) {

	    return adminService.addSilk(session, silk, result, images);
	}
	
	@GetMapping("/manage-silk")
	public String viewSilk(HttpSession session,ModelMap map) {
		return adminService.viewSilk(session,map);
	}
	
	@GetMapping("/delete-silk/{id}")
	public String deleteSilk(HttpSession session,@PathVariable int id) {
		return adminService.deleteSilk(session,id);
	}
	
	@GetMapping("/edit-silk/{id}")
	public String editSilk(HttpSession session,@PathVariable int id,ModelMap map) {
		return adminService.editSilk(session,id,map);
	}
	
	@PostMapping("/edit-silk")
	public String updateSilk(HttpSession session,@Valid Silk silk,BindingResult result,List<MultipartFile> images,@RequestParam(required = false) List<String> removeImages) {
	    return adminService.updateSilk(session,silk,result,images,removeImages);
	}

	// =========================================================
	// ORDERS
	// =========================================================

	@GetMapping("/orders")
	public String viewOrders(HttpSession session, ModelMap map) {
		return adminService.viewOrders(session, map);
	}

	@PostMapping("/orders/{id}/cancel")
	public String cancelOrder(HttpSession session, @PathVariable int id, @RequestParam(required = false) String reason) {
		return adminService.cancelOrder(session, id, reason);
	}

	@PostMapping("/orders/{id}/status")
	public String updateOrderStatus(HttpSession session, @PathVariable int id, @RequestParam String status) {
		return adminService.updateOrderStatus(session, id, status);
	}
	
	

	
	
	
	
	
	
	

}
