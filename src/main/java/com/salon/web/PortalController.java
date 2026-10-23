package com.salon.web;

import com.salon.model.AppUser;
import com.salon.model.Appointment;
import com.salon.model.Client;
import com.salon.model.Feedback;
import com.salon.repository.AppUserRepository;
import com.salon.repository.AppointmentRepository;
import com.salon.repository.ClientRepository;
import com.salon.repository.FeedbackRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.nio.charset.StandardCharsets;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PortalController {
    private final AppUserRepository users;
    private final AppointmentRepository appointments;
    private final ClientRepository clients;
    private final FeedbackRepository feedback;
    private final PasswordEncoder passwordEncoder;

    public PortalController(
            AppUserRepository users,
            AppointmentRepository appointments,
            ClientRepository clients,
            FeedbackRepository feedback,
            PasswordEncoder passwordEncoder) {
        this.users = users;
        this.appointments = appointments;
        this.clients = clients;
        this.feedback = feedback;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    @Transactional
    public String registerClient(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String email,
            @RequestParam(defaultValue = "") String phone,
            @RequestParam(defaultValue = "") String password,
            @RequestParam(defaultValue = "") String confirmPassword,
            RedirectAttributes redirectAttributes) {
        String normalizedName = name.trim();
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String normalizedPhone = phone.trim();
        int passwordBytes = password.getBytes(StandardCharsets.UTF_8).length;

        if (normalizedName.isBlank() || normalizedName.length() > 120
                || !normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                || normalizedEmail.length() > 180
                || normalizedPhone.isBlank() || normalizedPhone.length() > 40) {
            redirectAttributes.addFlashAttribute("error", "Enter a valid name, email, and phone number.");
            return "redirect:/register";
        }
        if (password.length() < 8 || passwordBytes > 72) {
            redirectAttributes.addFlashAttribute("error", "Use a password between 8 and 72 UTF-8 bytes.");
            return "redirect:/register";
        }
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "The passwords do not match.");
            return "redirect:/register";
        }
        if (users.findByUsername(normalizedEmail).isPresent()
                || clients.findByEmail(normalizedEmail).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "An account with that email already exists.");
            return "redirect:/register";
        }

        Client client = clients.save(new Client(normalizedName, normalizedEmail, normalizedPhone));
        users.save(new AppUser(normalizedEmail, passwordEncoder.encode(password), "CLIENT", client));
        redirectAttributes.addFlashAttribute("message", "Account created. Sign in with your email and password.");
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(org.springframework.security.core.Authentication authentication) {
        AppUser account = users.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Signed-in account was not found"));
        return "redirect:" + ("OWNER".equals(account.getRole()) ? "/owner" : "/client");
    }

    @GetMapping("/owner")
    public String ownerDashboard(Model model) {
        model.addAttribute("clients", clients.findAll());
        model.addAttribute("feedback", feedback.findAllByOrderBySubmittedAtDesc());
        Map<LocalDate, List<Appointment>> appointmentsByDay = new TreeMap<>();
        for (Appointment appointment : appointments.findAllByAppointmentAtGreaterThanEqualOrderByAppointmentAtAsc(
                LocalDateTime.now())) {
            appointmentsByDay.computeIfAbsent(
                    appointment.getAppointmentAt().toLocalDate(), day -> new ArrayList<>())
                    .add(appointment);
        }
        model.addAttribute("appointmentsByDay", appointmentsByDay);
        return "owner";
    }

    @PostMapping("/owner/clients")
    @Transactional
    public String addClient(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            RedirectAttributes redirectAttributes) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (name.isBlank() || normalizedEmail.isBlank() || phone.isBlank() || password.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Complete all fields and use a password of at least 8 characters.");
            return "redirect:/owner";
        }
        if (users.findByUsername(normalizedEmail).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "An account with that email already exists.");
            return "redirect:/owner";
        }

        Client client = clients.save(new Client(name.trim(), normalizedEmail, phone.trim()));
        users.save(new AppUser(normalizedEmail, passwordEncoder.encode(password), "CLIENT", client));
        redirectAttributes.addFlashAttribute("message", "Client account created.");
        return "redirect:/owner";
    }

    @GetMapping("/client")
    public String clientDashboard(
            org.springframework.security.core.Authentication authentication,
            Model model) {
        AppUser account = users.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Signed-in account was not found"));
        Client client = account.getClient();
        if (client == null) {
            throw new IllegalStateException("Client account has no client profile");
        }
        model.addAttribute("client", client);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("appointments", appointments.findByClientOrderByAppointmentAtDesc(client));
        model.addAttribute("feedback", feedback.findByClientOrderBySubmittedAtDesc(client));
        return "client";
    }

    @PostMapping("/client/appointments")
    public String bookAppointment(
            org.springframework.security.core.Authentication authentication,
            @RequestParam String service,
            @RequestParam String date,
            @RequestParam String time,
            RedirectAttributes redirectAttributes) {
        AppUser account = users.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Signed-in account was not found"));
        if (account.getClient() == null) {
            throw new IllegalStateException("Client account has no client profile");
        }

        String normalizedService = service.trim();
        if (normalizedService.isBlank() || normalizedService.length() > 120) {
            redirectAttributes.addFlashAttribute("error", "Enter a service name of up to 120 characters.");
            return "redirect:/client";
        }

        LocalDateTime appointmentAt;
        try {
            appointmentAt = LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(time));
        } catch (DateTimeParseException exception) {
            redirectAttributes.addFlashAttribute("error", "Choose a valid appointment date and time.");
            return "redirect:/client";
        }
        if (!appointmentAt.isAfter(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "Choose an appointment date and time in the future.");
            return "redirect:/client";
        }

        appointments.save(new Appointment(account.getClient(), normalizedService, appointmentAt));
        redirectAttributes.addFlashAttribute("message", "Your appointment has been booked.");
        return "redirect:/client";
    }

    @PostMapping("/client/feedback")
    public String submitFeedback(
            org.springframework.security.core.Authentication authentication,
            @RequestParam int rating,
            @RequestParam String message,
            RedirectAttributes redirectAttributes) {
        AppUser account = users.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Signed-in account was not found"));
        if (account.getClient() == null) {
            throw new IllegalStateException("Client account has no client profile");
        }
        if (rating < 1 || rating > 5 || message.isBlank() || message.length() > 1000) {
            redirectAttributes.addFlashAttribute("error", "Choose a rating from 1 to 5 and enter feedback (up to 1000 characters).");
            return "redirect:/client";
        }
        feedback.save(new Feedback(account.getClient(), rating, message.trim()));
        redirectAttributes.addFlashAttribute("message", "Thanks for sharing your feedback.");
        return "redirect:/client";
    }
}
