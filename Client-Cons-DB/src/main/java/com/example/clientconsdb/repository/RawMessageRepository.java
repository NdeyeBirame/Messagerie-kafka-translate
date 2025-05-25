package com.example.clientconsdb.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.clientconsdb.model.RawMessage;

public interface RawMessageRepository extends JpaRepository<RawMessage, Long> {
}
