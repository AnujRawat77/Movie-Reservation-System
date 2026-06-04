package com.movie_reservation.MovieReservationSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MovieReservationSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(MovieReservationSystemApplication.class, args);
	}

}
