package com.web.agua_facil.services;

import java.util.List;

import com.web.agua_facil.models.Reading;


public interface ReadingService {
	
	public Reading registerReading(Reading reading);
	public List<Reading> getAllReadings();
	public List<Reading> getReadingsByProperty(Long propertyId);
	List<Reading> getReadingsByClient(Long clienteId);
	public Reading findReadingById(Long id);
	Reading updateReading(Long id, Reading readingDetails);
	public void deleteReading(Long id);
	
}
