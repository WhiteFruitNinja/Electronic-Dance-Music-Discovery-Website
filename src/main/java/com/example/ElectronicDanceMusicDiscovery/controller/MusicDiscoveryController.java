package com.example.ElectronicDanceMusicDiscovery.controller;

import com.example.ElectronicDanceMusicDiscovery.model.Discogs;
import com.example.ElectronicDanceMusicDiscovery.service.DiscogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Controller
public class MusicDiscoveryController {

    @Autowired
    private DiscogsService discogsService;

    @GetMapping("/")
    public String mainPage(Model model){

        List<Integer> musicList = discogsService.getListOfReleaseId("Happy+Hardcore");

        Random randomizer = new Random();

        // Select random id from the music list
        int releaseId = musicList.get(randomizer.nextInt(musicList.size()));

        Map<String, Object> releaseData = discogsService.getReleaseData(releaseId);

        // Assuming the structure contains the following fields
        String title = (String) releaseData.get("title");
        List<Map<String, Object>> artists = (List<Map<String, Object>>) releaseData.get("artists");


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

        // Extract videos (if any)
        List<Map<String, Object>> videos = (List<Map<String, Object>>) releaseData.get("videos");

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

        return "musicList";
    }
}
