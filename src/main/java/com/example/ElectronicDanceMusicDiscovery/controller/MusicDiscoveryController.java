package com.example.ElectronicDanceMusicDiscovery.controller;

import com.example.ElectronicDanceMusicDiscovery.model.Discogs;
import com.example.ElectronicDanceMusicDiscovery.service.DiscogsService;
import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class MusicDiscoveryController {

    @Autowired
    private DiscogsService discogsService;

    @GetMapping("/")
    public String mainPage(Model model,@RequestParam(name = "releaseId", required = false) Integer releaseId,
                           HttpServletResponse response, @CookieValue(value = "historyReleases", required = false) String cookieValue,
                           HttpServletRequest request){

        String ipAddress = request.getRemoteAddr();
        String userAgentString = request.getHeader("User-Agent");

        // Parsing the User-Agent string
        UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);

        String os = userAgent.getOperatingSystem().getName();
        String browser = userAgent.getBrowser().getName();

        System.out.println("\n" + "IP address: " + ipAddress + "\n" + "OS: " + os + "\n" + "Browser: " + browser + "\n");

        List<Integer> musicList = discogsService.getListOfReleaseId("Happy+Hardcore");
        if (releaseId == null){
            Random randomizer = new Random();

            // Select random id from the music list
            releaseId = musicList.get(randomizer.nextInt(musicList.size()));

            return "redirect:/?releaseId=" + releaseId;

        }

        // if cookie's value is null, then set the current cookie's value to music id. If it has any value, add more music ids.
        if (cookieValue == null || cookieValue.isEmpty()) {
            Cookie newCookie = new Cookie("historyReleases", releaseId.toString());
            newCookie.setMaxAge(Integer.MAX_VALUE);
            newCookie.setPath("/");
            response.addCookie(newCookie);
        } else {
            String updatedValue = cookieValue + "|" + releaseId;
            Cookie updatedCookie = new Cookie("historyReleases", updatedValue);
            updatedCookie.setMaxAge(Integer.MAX_VALUE);
            updatedCookie.setPath("/");
            response.addCookie(updatedCookie);
        }

        String[] releaseIds = null;

        // Retrieve all release IDs from cookie
        if (cookieValue != null){
            releaseIds = cookieValue.split("\\|");
        }

        List<Map<String, Object>> historicalReleaseDataList = new ArrayList<>();

        // Check if releaseIds is not null and has elements
        if (releaseIds != null && releaseIds.length > 0) {
            int maxLengthOfReleaseIds = releaseIds.length;
            // Ensure minLengthOfReleaseIds is not out of bounds
            int minLengthOfReleaseIds = Math.max(0, releaseIds.length - 5);

            // Fetch data for each historical release
            for (int i = minLengthOfReleaseIds; i < maxLengthOfReleaseIds; i++) {
                try {
                    int idInt = Integer.parseInt(releaseIds[i].trim());
                    Map<String, Object> releaseData = discogsService.getReleaseData(idInt);
                    historicalReleaseDataList.add(releaseData);
                } catch (NumberFormatException e) {
                    // Handle the error if the ID is not a valid integer
                    System.err.println("Failed to parse release ID: " + releaseIds[i]);
                    e.printStackTrace();
                } catch (Exception e) {
                    // Handle any other exceptions thrown by getReleaseData
                    System.err.println("An error occurred while fetching release data for ID: " + releaseIds[i]);
                    e.printStackTrace();
                }
            }
        } else {
            // Handle the case where releaseIds is null or empty
            System.out.println("No release IDs found in the cookie.");
        }

        // Reverse the order of list
        Collections.reverse(historicalReleaseDataList);

        Random nextRandomizer = new Random();
        int nextReleaseId = musicList.get(nextRandomizer.nextInt(musicList.size()));

        Map<String, Object> releaseData = discogsService.getReleaseData(releaseId);

        // Assuming the structure contains the following fields
        String title = (String) releaseData.get("title");
        List<Map<String, Object>> artists = (List<Map<String, Object>>) releaseData.get("artists");
        String releaseDate = (String) releaseData.get("released_formatted");


        // Getting the artist names
        List<String> artistNames = artists.stream()
                .map(artist -> (String) artist.get("name"))
                .collect(Collectors.toList());


        // Add data to the model
        model.addAttribute("musicList", musicList);
        model.addAttribute("releaseData", releaseData);
        model.addAttribute("releaseTitle", title);
        model.addAttribute("artistNames", artistNames);
        model.addAttribute("releaseId", releaseId);
        model.addAttribute("releaseDate", releaseDate);
        model.addAttribute("historicalReleaseData", historicalReleaseDataList);

        model.addAttribute("nextReleaseId", nextReleaseId);

        // Extract videos (if any)
        List<Map<String, Object>> videos = (List<Map<String, Object>>) releaseData.get("videos");

        // Extract Images (if any)
        List<Map<String, Object>> images = (List<Map<String, Object>>) releaseData.get("images");


        // Modify the URLs for embedding
        if (videos != null) {
            for (Map<String, Object> video : videos) {
                String originalUrl = (String) video.get("uri");

                if (originalUrl != null && originalUrl.contains("watch?v=")) {
                    // Extract the video ID from the original URL
                    String videoId = originalUrl.substring(originalUrl.indexOf("=") + 1);

                    // Construct the new embed URL
                    String embedUrl = "https://www.youtube.com/embed/" + videoId;

                    // Set the new URL back to the video map
                    video.put("embedUrl", embedUrl);
                }
            }
        }

        model.addAttribute("videos", videos);
        model.addAttribute("images", images);

        return "index";
    }

    //Todo fix
    @GetMapping("/history")
    public String historyOfReleasesPage(Model model, @CookieValue(value = "historyReleases", required = false) String cookieValue,
                                        @RequestParam(defaultValue = "1",name = "page", required = false) Integer page,
                                        @RequestParam(name = "q", required = false) String query){

        String[] releaseIds = null;

        // Retrieve all release IDs from cookie
        if (cookieValue != null){
            releaseIds = cookieValue.split("\\|");
        }


        model.addAttribute("currentPage", page);


        List<Map<String, Object>> historicalReleaseDataList = new ArrayList<>();

        // Check if releaseIds is not null and has elements
        if (releaseIds != null && releaseIds.length > 0 && page >= 1) {
            int maxLengthOfReleaseIds = releaseIds.length - ((page - 1) * 5);
            // Ensure minLengthOfReleaseIds is not out of bounds
            int minLengthOfReleaseIds = Math.max(0, maxLengthOfReleaseIds - 5);

            if(maxLengthOfReleaseIds >= 0){
                // Fetch data for each historical release
                if (query == null) {
                    for (int i = minLengthOfReleaseIds; i < maxLengthOfReleaseIds; i++) {
                        try {
                            int idInt = Integer.parseInt(releaseIds[i].trim());
                            Map<String, Object> releaseData = discogsService.getReleaseData(idInt);
                            historicalReleaseDataList.add(releaseData);
                        } catch (NumberFormatException e) {
                            // Handle the error if the ID is not a valid integer
                            System.err.println("Failed to parse release ID: " + releaseIds[i]);
                            e.printStackTrace();
                        } catch (Exception e) {
                            // Handle any other exceptions thrown by getReleaseData
                            System.err.println("An error occurred while fetching release data for ID: " + releaseIds[i]);
                            e.printStackTrace();
                        }
                    }
                }
                else{
                    query = query.toLowerCase();
                    for (int i = maxLengthOfReleaseIds - 1; i > 0; i--){

                        int delay = 1000; // number of milliseconds to sleep

                        long start = System.currentTimeMillis();
                        while(start >= System.currentTimeMillis() - delay); // do nothing

                        System.out.println("Time Slept: " + Long.toString(System.currentTimeMillis() - start));

                        if(historicalReleaseDataList.size() == 5){
                            break;
                        }

                        int idInt = Integer.parseInt(releaseIds[i].trim());

                        Map<String, Object> releaseData = discogsService.getReleaseData(idInt);
                        String title = (String) releaseData.get("title");
                        title = title.toLowerCase();

                        System.out.println(idInt);

                        if (!title.toLowerCase().contains(query)){
                            continue;
                        }

                        historicalReleaseDataList.add(releaseData);
                    }
                }
            } else {
                // Handle the case where releaseIds is null or empty
                System.out.println("No release IDs found in the cookie.");
            }
        }

        // Define page size
        int pageSize = 5;
        int totalReleases = historicalReleaseDataList.size();

        // Calculate total pages
        int totalPages = (int) Math.ceil((double) totalReleases / pageSize);

        // Ensure the page number is within bounds
        page = Math.max(1, Math.min(page, totalPages)); // Clamp page to valid range

        // Calculate start and end index for pagination
        int startIndex = Math.max(((page - 1) * pageSize), 0);
        int endIndex = Math.min(startIndex + pageSize, totalReleases);

        // Get the sublist for the current page
        List<Map<String, Object>> paginatedReleases = (startIndex < totalReleases)
                ? historicalReleaseDataList.subList(startIndex, endIndex)
                : new ArrayList<>();



        int maxPages = releaseIds != null ? (int) Math.ceil((double) releaseIds.length / pageSize) : 0;


        Random nextRandomizer = new Random();
        List<Integer> musicList = discogsService.getListOfReleaseId("Happy+Hardcore");
        int nextReleaseId = musicList.get(nextRandomizer.nextInt(musicList.size()));



        model.addAttribute("maxPages", maxPages);
        model.addAttribute("historicalReleaseData", paginatedReleases);
        model.addAttribute("nextReleaseId", nextReleaseId);

        return "history";
    }



}
