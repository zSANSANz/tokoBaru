package com.tkbaru.web;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tkbaru.common.Constants;
import com.tkbaru.model.Items;
import com.tkbaru.model.LoginContext;
import com.tkbaru.model.SalesOrder;
import com.tkbaru.service.CustomerMenuService;
import com.tkbaru.service.LookupService;
import com.tkbaru.service.SalesOrderService;
import com.tkbaru.service.TruckVendorService;

@Controller
@RequestMapping("customer/delivery/confirmation")
public class CustomerMenuController {
	private static final Logger logger = LoggerFactory.getLogger(CustomerMenuController.class);
	
	@Autowired
	CustomerMenuService CustomerMenuManager;
	
	@Autowired
	SalesOrderService salesManager;
	
	@Autowired
	TruckVendorService truckVendorManager;
	
	@Autowired
	LookupService lookupManager;
	
	@Autowired
	private LoginContext loginContextSession;
	
	@RequestMapping(method=RequestMethod.GET)
	public String customerMenuLoad(Locale locale, Model model) {	
		logger.info("[customerMenuLoad] " + "");
		
		List<SalesOrder> soList = CustomerMenuManager.getSalesOrderWithDeliverId();
		
		model.addAttribute("CustomerMenuList", soList);
				
		model.addAttribute(Constants.SESSIONKEY_LOGINCONTEXT, loginContextSession);
		model.addAttribute(Constants.PAGEMODE, Constants.PAGEMODE_PAGELOAD);
		model.addAttribute(Constants.ERRORFLAG, Constants.ERRORFLAG_HIDE);
		
		model.addAttribute(Constants.PAGE_TITLE, "");

		return Constants.JSPPAGE_CUSTOMER_DELIVERY;
	}
	
	
	@RequestMapping(value = "/loaddeliver/{salesId}", method = RequestMethod.GET)
	public String loadDeliver(Locale locale, 
								Model model,
								@PathVariable int salesId) {
		logger.info("[loadDeliver] : " + ", salesId: " + salesId);

		SalesOrder selectedSoObject = salesManager.getSalesOrderById(salesId); 

		model.addAttribute("customerMenuSOForm", selectedSoObject);

		model.addAttribute("cmDDL", lookupManager.getLookupByCategory(Constants.LOOKUPCATEGORY_UNIT));
		model.addAttribute(Constants.SESSIONKEY_LOGINCONTEXT, loginContextSession);
		model.addAttribute(Constants.PAGEMODE, Constants.PAGEMODE_EDIT);
		model.addAttribute(Constants.ERRORFLAG, Constants.ERRORFLAG_HIDE);
		
		model.addAttribute(Constants.PAGE_TITLE, "");

		return Constants.JSPPAGE_CUSTOMER_DELIVERY;
	}

	@RequestMapping(value="/savedeliver/{salesId}", method=RequestMethod.POST)
	public String saveDeliver(Locale locale,
										Model model,
										@ModelAttribute("customerMenuSOForm") SalesOrder customerMenuSO,
										RedirectAttributes redirectAttributes,
										@PathVariable int salesId) {	
		logger.info("[saveDeliver] " + "editDeliver: " + customerMenuSO.toString());

		SalesOrder so = salesManager.getSalesOrderById(salesId);
		
		for (Items soI:so.getItemsList()) {
			for (Items csoI:customerMenuSO.getItemsList()) {
				if (soI.getItemsId() == csoI.getItemsId()) {
					soI.getDeliverList().get(0).setNet(csoI.getDeliverList().get(0).getNet());
					soI.getDeliverList().get(0).setBaseNet(csoI.getDeliverList().get(0).getNet() * soI.getToBaseValue());
					
					soI.getDeliverList().get(0).setTare(soI.getDeliverList().get(0).getBruto() - csoI.getDeliverList().get(0).getNet());
					soI.getDeliverList().get(0).setBaseTare((soI.getDeliverList().get(0).getBruto() - csoI.getDeliverList().get(0).getNet()) * soI.getToBaseValue());
					
					soI.getDeliverList().get(0).setUpdatedBy(loginContextSession.getUserLogin().getUserId());
					soI.getDeliverList().get(0).setUpdatedDate(new Date());
				}
			}
		}
		
		CustomerMenuManager.editDeliver(so); 
		
		redirectAttributes.addFlashAttribute(Constants.PAGEMODE, Constants.PAGEMODE_LIST);
		redirectAttributes.addFlashAttribute(Constants.ERRORFLAG, Constants.ERRORFLAG_HIDE);
		
		return "redirect:/customer/delivery/confirmation";
	}
	
}
