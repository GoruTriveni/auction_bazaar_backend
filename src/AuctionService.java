package com.example.AuctionBazaar.service;

import com.example.AuctionBazaar.Model.Auction;
import com.example.AuctionBazaar.Model.User;
import com.example.AuctionBazaar.repository.AuctionRepository;
import com.example.AuctionBazaar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuctionService {
    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository; // To fetch all users

    @Autowired
    private EmailService emailService;


    // Create auction with required fields including ownerId
    public Auction createAuction(Auction auction) {
        // Ensure auction has an ownerId before saving it
        // This could also be checked/validated further based on the user (logged-in user)
        if (auction.getOwnerId() == null) {
            throw new IllegalArgumentException("Owner ID cannot be null.");
        }

        Auction savedAuction = auctionRepository.save(auction);

        // Prepare email details
        String auctionTitle = savedAuction.getTitle();
        String auctionTime = savedAuction.getStartTime().toString(); // Format this if needed
        String emailSubject = "New Auction Created!";
        String emailBody = String.format("Hello,\n\nA new auction '%s' is scheduled for %s.\n\nDon't miss out!\n\nBest Regards,\nAuctionBazaar Team",
                auctionTitle, auctionTime);

        // Fetch all users and send emails
        List<User> users = userRepository.findAll(); // Ensure UserRepository is implemented

        for (User user : users) {
            try {
                emailService.sendEmail(user.getEmail(), emailSubject, emailBody);
            } catch (Exception e) {
                // Log the error and continue with the next user
                System.err.println("Failed to send email to " + user.getEmail() + ": " + e.getMessage());
            }
        }

        return savedAuction;
    }

    public Optional<Auction> updateAuction(Long auctionId, Auction updatedAuction) {
        return auctionRepository.findById(auctionId).map(auction -> {
            auction.setTitle(updatedAuction.getTitle());
            auction.setDescription(updatedAuction.getDescription());
            auction.setStartPrice(updatedAuction.getStartPrice());
            auction.setEndTime(updatedAuction.getEndTime());
            return auctionRepository.save(auction);
        });
    }

    public void deleteAuction(Long auctionId) {
        auctionRepository.deleteById(auctionId);
    }
}
