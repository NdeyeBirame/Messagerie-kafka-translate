package com.example.clientconsdb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.clientconsdb.model.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {
    boolean existsByNom(String nom);
    Optional<Client> findByNom(String nom);
    @Override
    List<Client> findAll();
}
