package com.eco.emission_estimator.controller;

import com.eco.emission_estimator.model.ApplianceLog;
import com.eco.emission_estimator.model.User;
import com.eco.emission_estimator.repository.ApplianceLogRepository;
import com.eco.emission_estimator.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class EmissionController {

    private final UserRepository userRepo;
    private final ApplianceLogRepository logRepo;

    public EmissionController(UserRepository userRepo, ApplianceLogRepository logRepo) {
        this.userRepo = userRepo;
        this.logRepo = logRepo;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already registered"));
        }
        User saved = userRepo.save(user);
        saved.setPassword(null);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
        Optional<User> userOpt = userRepo.findByEmail(creds.get("email"));
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(creds.get("password"))) {
            User user = userOpt.get();
            user.setPassword(null);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid email or password"));
    }

    @PutMapping("/household/{userId}")
    public ResponseEntity<?> updateHousehold(@PathVariable Long userId, @RequestBody User details) {
        return userRepo.findById(userId).map(user -> {
            if (details.getFamilyMembers() != null) user.setFamilyMembers(details.getFamilyMembers());
            if (details.getHouseholdType() != null) user.setHouseholdType(details.getHouseholdType());
            if (details.getCity() != null) user.setCity(details.getCity());
            User updated = userRepo.save(user);
            updated.setPassword(null);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/appliances")
    public ResponseEntity<ApplianceLog> addAppliance(@RequestBody ApplianceLog log) {
        log.calculateEnergyAndEmissions();
        return ResponseEntity.ok(logRepo.save(log));
    }

    @DeleteMapping("/appliances/{id}")
    public ResponseEntity<?> deleteAppliance(@PathVariable Long id) {
        logRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", id));
    }

    @GetMapping("/report/{userId}")
    public ResponseEntity<?> getMonthlyReport(@PathVariable Long userId) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        List<ApplianceLog> logs = logRepo.findByUserIdOrderByMonthYearAsc(userId);

        Map<String, Double> monthlyKwhMap = new LinkedHashMap<>();
        Map<String, Double> monthlyCo2Map = new LinkedHashMap<>();
        ApplianceLog highestConsumer = null;

        for (ApplianceLog log : logs) {
            monthlyKwhMap.merge(log.getMonthYear(), log.getMonthlyKwh(), Double::sum);
            monthlyCo2Map.merge(log.getMonthYear(), log.getCo2Kg(), Double::sum);
            if (highestConsumer == null || log.getCo2Kg() > highestConsumer.getCo2Kg()) {
                highestConsumer = log;
            }
        }

        monthlyKwhMap.replaceAll((k, v) -> Math.round(v * 100.0) / 100.0);
        monthlyCo2Map.replaceAll((k, v) -> Math.round(v * 100.0) / 100.0);

        double totalCo2 = monthlyCo2Map.values().stream().mapToDouble(Double::doubleValue).sum();
        double perCapitaCo2 = user.getFamilyMembers() > 0
                ? Math.round((totalCo2 / user.getFamilyMembers()) * 100.0) / 100.0
                : totalCo2;

        List<String> suggestionKeys = buildSuggestionKeys(highestConsumer, perCapitaCo2);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("logs", logs);
        response.put("monthlyKwh", monthlyKwhMap);
        response.put("monthlyCo2", monthlyCo2Map);
        response.put("totalCo2Kg", Math.round(totalCo2 * 100.0) / 100.0);
        response.put("perCapitaCo2Kg", perCapitaCo2);
        response.put("emissionFactor", ApplianceLog.INDIA_GRID_EMISSION_FACTOR);
        response.put("suggestionKeys", suggestionKeys);

        return ResponseEntity.ok(response);
    }

    private List<String> buildSuggestionKeys(ApplianceLog top, double perCapitaCo2) {
        List<String> keys = new ArrayList<>();
        if (top != null) {
            String name = top.getApplianceName().toLowerCase();
            if (name.contains("ac") || name.contains("air")) keys.add("TIP_AC");
            else if (name.contains("fridge") || name.contains("refrigerator")) keys.add("TIP_FRIDGE");
            else if (name.contains("fan")) keys.add("TIP_FAN");
            else if (name.contains("light") || name.contains("bulb")) keys.add("TIP_LED");
            else if (name.contains("washing")) keys.add("TIP_WASHING");
        }
        if (perCapitaCo2 > 100.0) keys.add("TIP_SOLAR");
        keys.add("TIP_STANDBY");
        return keys;
    }
}