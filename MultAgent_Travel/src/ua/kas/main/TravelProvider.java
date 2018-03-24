package ua.kas.main;

import java.util.Hashtable;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class TravelProvider extends Agent {

	private static final long serialVersionUID = 1L;

	// The catalog of books for sale (maps the title of a book to its price)
	private Hashtable<String, String> catalogue;
	// The GUI by means of which the user can add books in the catalog
	private TravelProviderGui myGui;

	// Put agent initializations here
	protected void setup() {
		// Create the catalog
		catalogue = new Hashtable<String, String>();

		// Create and show the GUI
		myGui = new TravelProviderGui(this);
		myGui.showGui();

		// Register the travel-order service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("travel-order");
		sd.setName("JADE-book-trading");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Add the behavior serving queries from buyer agents
		addBehaviour(new OfferRequestsServer());

		// Add the behavior serving purchase orders from buyer agents
		addBehaviour(new PurchaseOrdersServer());
	}

	// Put agent clean-up operations here
	@Override
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Close the GUI
		myGui.dispose();

		// Printout a dismissal message
		System.out.println("Provider  " + getAID().getName() + " stop!");
	}

	/**
	 * This is invoked by the GUI when the user adds a new book for sale
	 */
	public void updateCatalogue(final String tittle, final String price, final int jb) {
		addBehaviour(new OneShotBehaviour() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				final String gb = price + "/" + jb;
				catalogue.put(tittle, gb);
				String[] hari = { "понедельник", "вторник", "среда", "четверг", "пятница", "cуббота", "воскресенье" };
				System.out
						.println(tittle + " departure " + hari[jb] + " put into the catalog with the cost = " + price);
			}
		});
	}

	/**
	 * Inner class OfferRequestsServer. This is the behaviour used by
	 * Book-seller agents to serve incoming requests for offer from buyer
	 * agents. If the requested book is in the local catalogue the seller agent
	 * replies with a PROPOSE message specifying the price. Otherwise a REFUSE
	 * message is sent back.
	 */

	private class OfferRequestsServer extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// CFP Message received. Process it
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();

				String price = catalogue.get(title);
				if (price != null) {
					// The requested book is available for sale. Reply with the
					// price
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(price);
				} else {
					// The requested book is NOT available for sale.
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
			} else {
				block();
			}
		}
	} // End of inner class OfferRequestsServer

	/**
	 * Inner class PurchaseOrdersServer. This is the behaviour used by
	 * Book-seller agents to serve incoming offer acceptances (i.e. purchase
	 * orders) from buyer agents. The seller agent removes the purchased book
	 * from its catalogue and replies with an INFORM message to notify the buyer
	 * that the purchase has been sucesfully completed.
	 */
	private class PurchaseOrdersServer extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// ACCEPT_PROPOSAL Message received. Process it
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();

				String price = catalogue.remove(title);
				if (price != null) {
					reply.setPerformative(ACLMessage.INFORM);
					System.out.println(title + " order from customers " + msg.getSender().getName());
				} else {
					// The requested book has been sold to another buyer in the
					// meanwhile .
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
			} else {
				block();
			}
		}
	} // End of inner class OfferRequestsServer
}