package com.example.AuctionBazaar.service;

import com.example.AuctionBazaar.Model.Auction;
import com.example.AuctionBazaar.Model.Bid;
import com.example.AuctionBazaar.repository.AuctionRepository;
import com.example.AuctionBazaar.repository.BidRepository;
import com.example.AuctionBazaar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.example.AuctionBazaar.Model.User;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ScheduledTask {

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private UserRepository userRepository;

    @Scheduled(cron = "0 */15 * * * *") // Runs every 15 minutes
    public void processEndedAuctions() {
        // Fetch all auctions that have ended but do not have a winner announced
        List<Auction> endedAuctions = auctionRepository.findByEndTimeBeforeAndWinnerUserIdIsNull(new java.sql.Timestamp(System.currentTimeMillis()));

        for (Auction auction : endedAuctions) {
            List<Bid> bids = bidRepository.findByAuctionId(auction.getAuctionId());

            if (!bids.isEmpty()) {
                // Find the highest bid
                Optional<Bid> highestBid = bids.stream()
                        .max(Comparator.comparing(Bid::getBidAmount));

                highestBid.ifPresent(bid -> {
                    // Set winner and update the auction
                    auction.setWinnerUserId(bid.getUserId());
                    auctionRepository.save(auction);

                    // Send winner announcement email
                    String email = getEmailByUserId(bid.getUserId()); // Fetch email from user service or repository
                    emailService.sendEmail(
                            email,
                            "Congratulations! You are the winner",
                            String.format("You have won the auction '%s' with a bid of %s.", auction.getTitle(), bid.getBidAmount())
                    );
                });
            } else {
                // No bids placed; winner remains null
                auction.setWinnerUserId(null);
                auctionRepository.save(auction);
            }
        }
    }

    // Mock method to fetch email by user ID (replace with actual logic)
    private String getEmailByUserId(Long userId) {
        // Fetch email from the user repository
        return userRepository.findById(Math.toIntExact(userId))
                .map(User::getEmail) // Extract the email if the user is found
                .orElseThrow(() -> new NoSuchElementException("User with ID " + userId + " not found"));
    }
    @Scheduled(cron = "0 0 9 * * ?") // Every day at 9 AM
    public void sendDailyUpcomingAuctionEmails() {
        // Fetch upcoming auctions and users from the database
        // Example logic:
        String email = "akashu38143@gmail.com";
        String auctionTitle = "Daily Vintage Art Auction";
        String auctionTime = "2024-12-01 10:00 AM";
        emailService.sendEmail(email,
                "Daily Auction Reminder",
                String.format("Reminder: Auction '%s' is happening at %s.", auctionTitle, auctionTime));
    }
}
