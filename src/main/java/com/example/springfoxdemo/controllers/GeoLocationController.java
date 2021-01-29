package com.example.springfoxdemo.controllers;

import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/geo")
public class GeoLocationController implements GeoLocationControllerOpenApi {

  @GetMapping(value = "/city/{id}")
  public ResponseEntity<GeoLocation> readGeoLocationByCityId(@PathVariable long id) {
    return ResponseEntity.of(Optional.of(new GeoLocation(1L, -23.5475000D, -46.6361100D)));
  }
}
