package it.prova.raccoltafilmspringmvc.web.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import it.prova.raccoltafilmspringmvc.model.Regista;
import it.prova.raccoltafilmspringmvc.service.RegistaService;

@Controller
@RequestMapping(value = "/regista")
public class RegistaController {

	@Autowired
	private RegistaService registaService;

	@GetMapping
	public ModelAndView listAllRegisti() {
		ModelAndView mv = new ModelAndView();
		List<Regista> registi = registaService.listAllElements();
		mv.addObject("registi_list_attribute", registi);
		mv.setViewName("regista/list");
		return mv;
	}

	@GetMapping("/insert")
	public String createRegista(Model model) {
		model.addAttribute("insert_regista_attr", new Regista());
		return "regista/insert";
	}

	@PostMapping("/save")
	public String saveRegista(@Valid @ModelAttribute("insert_regista_attr") Regista regista, BindingResult result,
			RedirectAttributes redirectAttrs) {
		if (result.hasErrors()) {
			return "regista/insert";
		}
		registaService.inserisciNuovo(regista);
		
		redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");
		return "redirect:/regista";
	}

	@GetMapping("/search")
	public String searchRegista() {
		return "regista/search";
	}

	@PostMapping("/list")
	public String listRegisti(Regista registaExample, ModelMap model) {
		List<Regista> registi = registaService.findByExample(registaExample);
		model.addAttribute("registi_list_attribute", registi);
		return "regista/list";
	}

	@GetMapping(value = "/searchRegistiAjax", produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody String searchRegista(@RequestParam String term) {

		List<Regista> listaRegistaByTerm = registaService.cercaByCognomeENomeILike(term);
		return buildJsonResponse(listaRegistaByTerm);
	}
	
	@GetMapping("/edit/{idRegista}")
	public String editRegista(@PathVariable(required = true) Long idRegista, Model model ) {
		model.addAttribute("update_regista_attr", registaService.caricaSingoloElemento(idRegista));

		return "regista/edit";
	}

	@PostMapping("/edit/saveupdate")
	public String saveEditRegista(@Valid @ModelAttribute("update_regista_attr") Regista regista, BindingResult result,
								  RedirectAttributes redirectAttrs) {

		if (result.hasErrors()) {
			return "regista/edit";
		}

		System.out.println("------------- DEBUG ----------"+regista.getId());
		registaService.aggiorna(regista);

		redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");

		return "redirect:/regista";
	}
	
	@GetMapping("/show/{idRegista}")
	public String showRegista(@PathVariable(required = true) Long idRegista, Model model) {
		model.addAttribute("show_regista_attr", registaService.caricaSingoloElemento(idRegista));

		return "regista/show";
	}

	@GetMapping("/delete/{idRegista}")
	public String prepareDeleteRegista(@PathVariable(required = true) Long idRegista, Model model) {
		model.addAttribute("delete_regista_attr", registaService.caricaSingoloElementoConFilms(idRegista));

		return "regista/delete";
	}

	@PostMapping("/delete/executedelete")
	public String executeDeleteRegista(@ModelAttribute("delete_regista_attr") Regista regista,  RedirectAttributes redirectAttrs) {

		try {
			registaService.rimuovi(registaService.caricaSingoloElementoConFilms(regista.getId()));
		} catch (RuntimeException e) {
			redirectAttrs.addFlashAttribute("errorMessage", "Non posso eliminare qusto regista.");
			return "redirect:/regista";
		}
		redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");

		return "redirect:/regista";
	}

	private String buildJsonResponse(List<Regista> listaRegisti) {
		JsonArray ja = new JsonArray();

		for (Regista registaItem : listaRegisti) {
			JsonObject jo = new JsonObject();
			jo.addProperty("value", registaItem.getId());
			jo.addProperty("label", registaItem.getNome() + " " + registaItem.getCognome());
			ja.add(jo);
		}

		return new Gson().toJson(ja);
	}

}
