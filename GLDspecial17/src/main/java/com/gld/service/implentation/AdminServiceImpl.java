package com.gld.service.implentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.gld.dto.Saree;
import com.gld.dto.Silk;
import com.gld.helper.CloudinaryHelper;
import com.gld.repository.SareeRepository;
import com.gld.repository.SilkRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Service
public class AdminServiceImpl implements AdminService{
	
	@Autowired
	Saree saree;
	
	@Autowired
	CloudinaryHelper cloudinaryHelper;
	
	@Autowired
	Silk silk;
	
	@Autowired
	SilkRepository silkRepository;
	
	
	@Autowired
	SareeRepository sareeRepository;

	@Override
	public String addSaree(HttpSession session, ModelMap map) {
		if(session.getAttribute("admin")!=null) {
			map.put("saree",saree );
			return "add-saree.html";
		}
		else {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}
		
	}

	public String addSaree(HttpSession session,@Valid Saree saree,BindingResult result,MultipartFile[] images) {

	    if (session.getAttribute("admin") != null) {

	        if (result.hasErrors()) {

	            return "add-saree.html";

	        } else {

	            List<String> imageLinks = new ArrayList<>();

	            for (MultipartFile image : images) {

	                if (!image.isEmpty()) {

	                    String imageLink = cloudinaryHelper.saveImage(image);

	                    imageLinks.add(imageLink);
	                }
	            }

	            saree.setImageLinks(imageLinks);

	            sareeRepository.save(saree);

	            session.setAttribute("success", "Saree Added Successfully");

	            return "redirect:/admin/home";
	        }

	    } else {

	        session.setAttribute("failure", "Invalid Session, Login Again");

	        return "redirect:/login";
	    }
	}

	@Override
	public String viewSaree(HttpSession session, ModelMap map) {
		if(session.getAttribute("admin")!=null)
		{
			List<Saree> saree=sareeRepository.findAll();
			if(saree.isEmpty()) {
				session.setAttribute("failure", "No Mobile cover Added Yet");
				return "redirect:/admin/home";
			}
			else {
				map.put("saree", saree);
				return "admin-sarees.html";
			}
		}
		else {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}

	@Override
	public String deleteSaree(HttpSession session, int id) {
		if(session.getAttribute("admin")!=null) {
			sareeRepository.deleteById(id);
			session.setAttribute("success", "Saree Deleted Success");
			return "redirect:/admin/manage-saree";
		}
		else {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}

	@Override
	public String editSaree(HttpSession session, int id, ModelMap map) {
		if(session.getAttribute("admin")!=null) {
			Saree saree=sareeRepository.findById(id).orElseThrow();
			map.put("saree", saree);
			return "edit-saree.html";
		}else {
			session.setAttribute("failure","Invalid Session, Login Again");
			return "redirect:/login";
		}
	}
	
	
	@Override
	public String addSilk(HttpSession session, ModelMap map) {
		if(session.getAttribute("admin")!=null) {
			map.put("silk",silk );
			return "add-silk.html";
		}
		else {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}
		
	}

	@Override
	public String addSilk(HttpSession session, @Valid Silk silk, BindingResult result, MultipartFile[] images) {
		if (session.getAttribute("admin") != null) {

	        if (result.hasErrors()) {

	            return "add-silk.html";

	        } else {

	            List<String> imageLinks = new ArrayList<>();

	            for (MultipartFile image : images) {

	                if (!image.isEmpty()) {

	                    String imageLink = cloudinaryHelper.saveImage(image);

	                    imageLinks.add(imageLink);
	                }
	            }

	            silk.setImageLinks(imageLinks);

	            silkRepository.save(silk);

	            session.setAttribute("success", "Silk Added Successfully");

	            return "redirect:/admin/home";
	        }

	    } else {

	        session.setAttribute("failure", "Invalid Session, Login Again");

	        return "redirect:/login";
	    }
	}

	@Override
	public String viewSilk(HttpSession session, ModelMap map) {
		if(session.getAttribute("admin")!=null)
		{
			List<Silk> silk=silkRepository.findAll();
			if(silk.isEmpty()) {
				session.setAttribute("failure", "No silk Added Yet");
				return "redirect:/admin/home";
			}
			else {
				map.put("silk", silk);
				return "admin-silk.html";
			}
		}
		else {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}

	@Override
	public String deleteSilk(HttpSession session, int id) {
		if(session.getAttribute("admin")!=null) {
			silkRepository.deleteById(id);
			session.setAttribute("success", "Silk Deleted Success");
			return "redirect:/admin/manage-silk";
		}
		else {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}

	@Override
	public String editSilk(HttpSession session, int id, ModelMap map) {
		if(session.getAttribute("admin")!=null) {
			Silk silk=silkRepository.findById(id).orElseThrow();
			map.put("silk", silk);
			return "edit-silk.html";
		}else {
			session.setAttribute("failure","Invalid Session, Login Again");
			return "redirect:/login";
		}
	}

	@Override
	public String updateSilk(HttpSession session, @Valid Silk silk, BindingResult result, List<MultipartFile> images,
			List<String> removeImages) {
		if (session.getAttribute("admin") == null) {

	        session.setAttribute("failure", "Invalid Session, Login Again");

	        return "redirect:/login";
	    }

	    if (result.hasErrors()) {

	        return "edit-silk.html";
	    }

	    Silk existingSilk = silkRepository.findById(silk.getId())
	            .orElseThrow();

	    List<String> imageLinks = existingSilk.getImageLinks();

	    if (imageLinks == null) {
	        imageLinks = new ArrayList<>();
	    }

	    // ==========================================
	    // REMOVE ONLY SELECTED IMAGES
	    // ==========================================

	    if (removeImages != null && !removeImages.isEmpty()) {

	        imageLinks.removeAll(removeImages);
	    }


	    // ==========================================
	    // ADD NEW IMAGES
	    // ==========================================

	    if (images != null) {

	        for (MultipartFile image : images) {

	            if (image != null && !image.isEmpty()) {

	                String imageUrl = cloudinaryHelper.saveImage(image);

	                imageLinks.add(imageUrl);
	            }
	        }
	    }


	    // ==========================================
	    // UPDATE SILK DETAILS
	    // ==========================================

	    existingSilk.setName(silk.getName());

	    existingSilk.setPrice(silk.getPrice());

	    existingSilk.setDummyprice(silk.getDummyprice());

	    existingSilk.setStock(silk.getStock());

	    existingSilk.setDescription(silk.getDescription());

	    existingSilk.setSilkType(silk.getSilkType());

	    existingSilk.setFabric(silk.getFabric());

	    existingSilk.setColor(silk.getColor());

	    existingSilk.setWeavingType(silk.getWeavingType());

	    existingSilk.setOrigin(silk.getOrigin());

	    existingSilk.setDesign(silk.getDesign());

	    existingSilk.setFinish(silk.getFinish());

	    existingSilk.setWeight(silk.getWeight());

	    existingSilk.setWidth(silk.getWidth());
	    
	    existingSilk.setPublished(
	            silk.isPublished()
	    );


	    // Save images

	    existingSilk.setImageLinks(imageLinks);


	    // Save Silk

	    silkRepository.save(existingSilk);


	    session.setAttribute(
	            "success",
	            "Silk Updated Successfully"
	    );

	    return "redirect:/admin/manage-silk";
	}

	@Override
	public String updateSaree(
	        HttpSession session,
	        @Valid Saree saree,
	        BindingResult result,
	        List<MultipartFile> images,
	        List<String> removeImages) {

	    // ==========================================
	    // CHECK ADMIN SESSION
	    // ==========================================

	    if (session.getAttribute("admin") == null) {

	        session.setAttribute(
	                "failure",
	                "Invalid Session, Login Again"
	        );

	        return "redirect:/login";
	    }


	    // ==========================================
	    // VALIDATION
	    // ==========================================

	    if (result.hasErrors()) {

	        return "edit-saree.html";
	    }


	    // ==========================================
	    // CHECK ID
	    // ==========================================

	    if (saree.getId() == 0) {

	        session.setAttribute(
	                "failure",
	                "Saree ID is missing"
	        );

	        return "redirect:/admin/manage-saree";
	    }


	    // ==========================================
	    // FIND EXISTING SAREE
	    // ==========================================

	    Optional<Saree> optionalSaree =
	            sareeRepository.findById(saree.getId());


	    if (optionalSaree.isEmpty()) {

	        session.setAttribute(
	                "failure",
	                "Saree not found with ID: " + saree.getId()
	        );

	        return "redirect:/admin/manage-saree";
	    }


	    Saree existingSaree =
	            optionalSaree.get();


	    // ==========================================
	    // GET EXISTING IMAGES
	    // ==========================================

	    List<String> imageLinks =
	            existingSaree.getImageLinks();


	    if (imageLinks == null) {

	        imageLinks = new ArrayList<>();
	    }


	    // ==========================================
	    // REMOVE SELECTED IMAGES
	    // ==========================================

	    if (removeImages != null &&
	        !removeImages.isEmpty()) {

	        imageLinks.removeAll(removeImages);
	    }


	    // ==========================================
	    // ADD NEW IMAGES
	    // ==========================================

	    if (images != null) {

	        for (MultipartFile image : images) {

	            if (image != null &&
	                !image.isEmpty()) {

	                String imageUrl =
	                        cloudinaryHelper.saveImage(image);

	                imageLinks.add(imageUrl);
	            }
	        }
	    }


	    // ==========================================
	    // UPDATE SAREE DETAILS
	    // ==========================================

	    existingSaree.setName(
	            saree.getName()
	    );

	    existingSaree.setPrice(
	            saree.getPrice()
	    );

	    existingSaree.setDummyprice(
	            saree.getDummyprice()
	    );

	    existingSaree.setStock(
	            saree.getStock()
	    );

	    existingSaree.setDescription(
	            saree.getDescription()
	    );

	    existingSaree.setSareeType(
	            saree.getSareeType()
	    );

	    existingSaree.setFabric(
	            saree.getFabric()
	    );

	    existingSaree.setColor(
	            saree.getColor()
	    );

	    existingSaree.setWeavingType(
	            saree.getWeavingType()
	    );

	    existingSaree.setOccasion(
	            saree.getOccasion()
	    );

	    existingSaree.setDesign(
	            saree.getDesign()
	    );

	    existingSaree.setBorderType(
	            saree.getBorderType()
	    );

	    existingSaree.setBlouseIncluded(
	            saree.isBlouseIncluded()
	    );
	    
	    existingSaree.setPublished(
	            saree.isPublished()
	    );

	    // ==========================================
	    // SAVE IMAGES
	    // ==========================================

	    existingSaree.setImageLinks(
	            imageLinks
	    );


	    // ==========================================
	    // SAVE
	    // ==========================================

	    sareeRepository.save(existingSaree);


	    // ==========================================
	    // SUCCESS
	    // ==========================================

	    session.setAttribute(
	            "success",
	            "Saree Updated Successfully"
	    );


	    return "redirect:/admin/manage-saree";
	}

	// =========================================================
	// ORDERS
	// =========================================================

	@Autowired
	private OrderService orderService;

	@Override
	public String viewOrders(HttpSession session, ModelMap map) {

		if (session.getAttribute("admin") == null) {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}

		map.put("orders", orderService.getAllOrders());
		return "admin-orders.html";
	}

	@Override
	public String cancelOrder(HttpSession session, int id, String reason) {

		if (session.getAttribute("admin") == null) {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}

		try {
			orderService.cancelOrderByAdmin(id, reason);
			session.setAttribute("success", "Order cancelled and the customer has been notified.");
		} catch (Exception e) {
			session.setAttribute("failure", e.getMessage());
		}

		return "redirect:/admin/orders";
	}

	@Override
	public String updateOrderStatus(HttpSession session, int id, String status) {

		if (session.getAttribute("admin") == null) {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}

		try {
			orderService.updateOrderStatus(id, status);
			session.setAttribute("success", "Order status updated to " + status + ".");
		} catch (Exception e) {
			session.setAttribute("failure", e.getMessage());
		}

		return "redirect:/admin/orders";
	}

}
