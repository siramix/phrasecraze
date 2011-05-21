package com.buzzwords;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author The BuzzWords Team
 *
 * The Game Manager is a class that will manage all aspects of the game scoring
 * and general bookkeeping. This is the go-to class for creating new games,
 * turns, and teams. The application shall also use this class for preparing
 * and retrieving cards from the virtual deck.
 */
public class GameManager
{
  /**
   * logging tag
   */
  public static String TAG = "GameManager";

  /**
   * The list of cardIds that we pull from (our "deck" of cards)
   */
  private Deck deck;

  /**
   * The position in the list of card ids (where we are in the "deck")
   */
  private int cardPosition;

  /**
   * List of team objects
   */
  private List<Team> teams; 
  private Iterator<Team> teamIterator;
  private Team currentTeam;
  
  /**
   * The maximum number of rounds for this game
   */
  private int numRounds;
  
  /**
   * The index of the round being played
   */
  private int currentRound;
  
  private int numTurns;
  private int currentTurn;

  /**
   * The card in play
   */
  private Card currentCard;

  
  
  /**
   * The set of cards that have been activated in the latest turn
   */
  private LinkedList<Card> currentCards;

  /**
   * An array indicating scoring for right, wrong, and skip (in that order)
   */
  private int[] rws_value_rules;
  
  /**
   * An array of resource IDs to each right, wrong, skip sprite
   */
  public final int[] rws_resourceIDs;
  
  /**
   * Time for the Timer in miliseconds
   */
  private int turn_time;

  /**
   * Standard Constructor
   * @param context - required for game to instantiate the database
   */
  public GameManager( Context context )
  {
    Log.d( TAG, "GameManager()" );
 
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    
    this.currentRound = 0;
    this.currentTurn = 0;
    this.currentCards = new LinkedList<Card>();
    this.rws_resourceIDs = new int[] {R.drawable.right, R.drawable.wrong, R.drawable.skip};
    
    this.turn_time = Integer.parseInt(sp.getString("turn_timer", "60")) * 1000;
    
    Log.d( TAG, "Turn time is " + turn_time );    
    this.rws_value_rules = new int[3];
    
    //Set score values for game
    this.rws_value_rules[0] = 1;  //Value for correct cards
    this.rws_value_rules[1] = -1; //Value for wrong cards
    this.rws_value_rules[2] = 0;  //set skip value to 0 if skip penalty is not on
    
    this.deck = new Deck(context);
  }

  /**
   * Get the card indicated by the cardIdPosition. If we've dealt past the end
   * of the deck, we should prep the deck.
   * @return the card we want
   */
  public Card getNextCard()
  {
    Log.d( TAG, "getNextCard()" );
    this.currentCard = this.deck.getCard();
    return this.currentCard;
  }

  /**
   * Return the previous card
   * @return the previous card in the deck
   */
  public Card getPreviousCard()
  {
    Log.d( TAG, "getPreviousCard()" );
    
    if( this.cardPosition == 0 )
    {
      this.cardPosition = 1; 
    }
    this.currentCard = this.currentCards.get(this.currentCards.size()-1);
    if( !this.currentCards.isEmpty() )
    {
      this.currentCards.removeLast();
    }
    return this.currentCard;
  }

  /**
   * Start the game given a set of team names. This creates both a game and
   * a set of teams in the database
   * @param teams - a string array of team names
   */
  public void StartGame( List<Team> teams, int rounds )
  {
    Log.d( TAG, "StartGame()" );
    this.teams = teams;
    Iterator<Team> itr = teams.iterator();
    for(itr = teams.iterator(); itr.hasNext();)
    {
      itr.next().setScore( 0 );
    }
    this.teamIterator = teams.iterator();
    this.currentTeam = teamIterator.next();
    this.numRounds = rounds;
    this.numTurns = this.teams.size()*this.numRounds;
    this.currentTurn++;
    this.deck.prepareForRound();
  }

  /**
   * Starts a new turn incrementing the round and/or team index as necessary.
   * This function also empties the collection of active cards.
   */
  public void NextTurn()
  {
    Log.d( TAG, "NextTurn()" );
    int score = this.currentTeam.getScore() + GetTurnScore();
    this.currentTeam.setScore( score );
    this.incrementActiveTeamIndex();
    this.currentCards.clear();
    this.currentTurn++;
    this.deck.prepareForRound();
  }
  
  public void incrementActiveTeamIndex()
  {
    if( this.teamIterator.hasNext() )
    {
      this.currentTeam = this.teamIterator.next();
    }
    else
    {
      this.teamIterator = this.teams.iterator();
      this.currentTeam = this.teamIterator.next();
      this.currentRound++;
    }
  }

  /**
   * Write turn and game relevant data to the database.
   */
  public void EndGame()
  {
    Log.d( TAG, "EndGame()" );
    int score = this.currentTeam.getScore() + GetTurnScore();
    this.currentTeam.setScore( score );
    this.teamIterator = this.teams.iterator();
  }

  /**
   * Adds the current card to the active cards
   * @param rws - the right, wrong, skip status
   */
  public void ProcessCard( int rws )
  {
    Log.d( TAG, "ProcessCard(" + rws + ")" );      
    this.currentCard.setRws( rws );
    this.currentCards.add( new Card(currentCard) );
  }
  
  /**
   * Return the card currently in play without moving through the deck
   * @return the card currently in play
   */
  public Card GetCurrentCard()
  {
    Log.d( TAG, "GetCurrentCard()" );
    return this.currentCard;
  }

  /**
   * Get a list of all cards that have been acted on in a given turn.
   * @return list of all cards
   */
  public LinkedList<Card> GetCurrentCards()
  {   
    Log.d( TAG, "GetCurrentCards()" );              
	  return this.currentCards;
  }

  /**
   * Iterate through through all cards for the current turn and return the
   * total score
   * @return score for the round
   */
  public int GetTurnScore()
  {
    Log.d( TAG, "GetTurnScore()" );              
    int ret = 0;
	  for( Iterator<Card> it = currentCards.iterator(); it.hasNext(); )
	  {
	    Card card = it.next();
	    ret += rws_value_rules[card.getRws()];
	  }
	  return ret;
  }

  /**
   * Return an array of scores representing a running score total.
   * @return Array of longs with an element for each team's latest total score.
   */
  public List<Team> GetTeams()
  {
    Log.d( TAG, "GetTeams()" );                  
	  return this.teams;
  }
  
  /**
   * Return the index of the team currently in play.
   * @return integer representing the index of the current team starting at 0.
   */
  public Team GetActiveTeam()
  {
    Log.d( TAG, "GetActiveTeamIndex()" );                      
    return this.currentTeam;
  }
  
  /**
   * Return the number of teams set up by the game manager.
   * @return integer representing the number of teams ie. the length of 
   * teamIds[]
   */
  public int GetNumTeams()
  {
    Log.d( TAG, "GetNumTeams()" );                          
	  return this.teams.size();
  }
  
  /**
   * Return the number of rounds that have fully taken place
   * @return int representing the number of rounds thus far in a game 
   */
  public int GetCurrentRound()
  {
    Log.d( TAG, "GetCurrentRound()" );                          
    return this.currentRound+1;
  }

  /**
   * Return the maximum number of rounds in this game
   * @return int representing the maximum number of rounds in this game
   */
  public int GetNumRounds()
  {
    Log.d( TAG, "GetNumRounds()" );                          
    return this.numRounds;
  }
  
  
  /**
   * Accessor to return the amount of time in each turn.
   * @return integer representing the number of miliseconds in each turn.
   */
  public int GetTurnTime()
  {
    Log.d( TAG, "GetTurnTime()" );    
    return this.turn_time;
  }
  
  public int GetNumberOfTurnsRemaining()
  {
    return this.numTurns-this.currentTurn;
  }  
}
