package com.web.agua_facil.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.web.agua_facil.models.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

}
