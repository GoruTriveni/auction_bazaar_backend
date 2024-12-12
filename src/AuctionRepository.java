package com.example.AuctionBazaar.repository;

import com.example.AuctionBazaar.Model.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    List<Auction> findByEndTimeBeforeAndWinnerUserIdIsNull(Timestamp currentTime);
}