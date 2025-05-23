package com.example.demo.controller;

import com.example.demo.dto.ReviewDto;
import com.example.demo.model.Part;
import com.example.demo.model.Review;
import com.example.demo.model.User;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.PartService;
import com.example.demo.service.ReviewService;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final PartService partService;
    private final UserService userService;

    public ReviewController(ReviewService reviewService, 
                            PartService partService,
                            UserService userService) {
        this.reviewService = reviewService;
        this.partService = partService;
        this.userService = userService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/add/{partId}")
    public String addReview(@PathVariable Long partId,
                            @Valid @ModelAttribute("review") ReviewDto reviewDto,
                            BindingResult result,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            Model model,
                            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            Optional<Part> optionalPart = partService.getPartById(partId);
            if (optionalPart.isEmpty()) {
                model.addAttribute("error", "Part not found.");
                return "parts/detail";
            }

            Part part = optionalPart.get();
            List<Review> reviews = reviewService.getReviewsByPart(partId);
            model.addAttribute("part", part);
            model.addAttribute("reviews", reviews);
            return "parts/detail";
        }

        User user = userDetails.getUser(); // ✅ Get actual User entity from CustomUserDetails
        reviewService.createReview(reviewDto, user, partId);
        redirectAttributes.addFlashAttribute("success", "Review submitted successfully!");
        return "redirect:/parts/" + partId;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/delete")
    public String deleteReview(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        User user = userDetails.getUser(); // ✅ Ensure you use the correct user
        Long partId = reviewService.deleteReview(id, user);
        redirectAttributes.addFlashAttribute("success", "Review deleted successfully!");
        return "redirect:/parts/" + partId;
    }
}
