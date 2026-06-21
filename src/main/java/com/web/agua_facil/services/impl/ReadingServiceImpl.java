package com.web.agua_facil.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.web.agua_facil.models.Reading;
import com.web.agua_facil.repositories.ReadingRepository;
import com.web.agua_facil.services.ReadingService;
import org.springframework.transaction.annotation.Transactional;


@Service
public class ReadingServiceImpl implements ReadingService {
	
	private final ReadingRepository readingRepository;
	
	public ReadingServiceImpl(ReadingRepository readingRepository) {
		this.readingRepository = readingRepository;
	}
	
	@Override
	@Transactional
    public Reading registerReading(Reading reading) {

        Optional<Reading> anteriorOpt = readingRepository.findFirstByPropertyIdAndDataLeituraBeforeOrderByDataLeituraDesc(
                reading.getProperty().getId(), 
                reading.getDataLeitura()
        );

        if (anteriorOpt.isPresent()) {
            Reading anterior = anteriorOpt.get();
            if (reading.getValorMedido() < anterior.getValorMedido()) {
                throw new IllegalArgumentException(
                    "Erro: O valor medido (" + reading.getValorMedido() + 
                    ") não pode ser menor que a leitura anterior (" + anterior.getValorMedido() + ")."
                );
            }
        }

        Optional<Reading> duplicada = readingRepository.findByPropertyIdAndDataLeitura(
                reading.getProperty().getId(), 
                reading.getDataLeitura()
        );

        if (duplicada.isPresent()) {
            throw new IllegalArgumentException("Já existe uma leitura registrada para este imóvel nesta data.");
        }

        return readingRepository.save(reading);
    }
    
	@Override
	public List<Reading> getAllReadings() {

	    return readingRepository.findAll();
	}

	@Override
	public List<Reading> getReadingsByProperty(Long propertyId) {
	    return readingRepository.findByPropertyIdOrderByDataLeituraDesc(propertyId);
	}
	
	@Override
	public List<Reading> getReadingsByClient(Long clienteId) {
	    return readingRepository.findByProperty_ClienteIdOrderByDataLeituraDesc(clienteId);
	}
	
	@Override
	public Reading findReadingById(Long id) {
	    return readingRepository.findById(id)
	            .orElseThrow(() -> new IllegalArgumentException("Leitura não encontrada com o ID: " + id));
	}
	
	@Transactional
	@Override
	public Reading updateReading(Long id, Reading readingDetails) {
	    Reading reading = findReadingById(id);

	    Optional<Reading> anteriorOpt = readingRepository.findFirstByPropertyIdAndDataLeituraBeforeOrderByDataLeituraDesc(
	            reading.getProperty().getId(), reading.getDataLeitura());
	    
	    if (anteriorOpt.isPresent() && readingDetails.getValorMedido() < anteriorOpt.get().getValorMedido()) {
	        throw new IllegalArgumentException("O valor atualizado não pode ser menor que a leitura anterior.");
	    }

	    reading.setValorMedido(readingDetails.getValorMedido());

	    return readingRepository.save(reading);
	}
	
	@Transactional
	@Override
	public void deleteReading(Long id) {

	    if (!readingRepository.existsById(id)) {
	        throw new IllegalArgumentException("Erro: Leitura com ID " + id + " não encontrada.");
	    }
	    	    
	    readingRepository.deleteById(id);
	}

}
	
	
