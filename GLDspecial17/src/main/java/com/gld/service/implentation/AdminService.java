package com.gld.service.implentation;

import java.util.List;

import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.gld.dto.Saree;
import com.gld.dto.Silk;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

public interface AdminService {

	String addSaree(HttpSession session, ModelMap map);

	String addSaree(HttpSession session, @Valid Saree saree, BindingResult result, MultipartFile[] images);

	String viewSaree(HttpSession session, ModelMap map);

	String deleteSaree(HttpSession session, int id);

	String editSaree(HttpSession session, int id, ModelMap map);

	String updateSaree(HttpSession session, @Valid Saree saree, BindingResult result, List<MultipartFile> images, List<String> removeImages);

	String addSilk(HttpSession session, ModelMap map);

	String addSilk(HttpSession session, @Valid Silk silk, BindingResult result, MultipartFile[] images);

	String viewSilk(HttpSession session, ModelMap map);

	String deleteSilk(HttpSession session, int id);

	String editSilk(HttpSession session, int id, ModelMap map);

	String updateSilk(HttpSession session, @Valid Silk silk, BindingResult result, List<MultipartFile> images,
			List<String> removeImages);

	// ===================== ORDERS =====================

	String viewOrders(HttpSession session, ModelMap map);

	String cancelOrder(HttpSession session, int id, String reason);

	String updateOrderStatus(HttpSession session, int id, String status);

}
