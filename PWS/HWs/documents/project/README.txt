We have packed all the 3 Netbeans projects (Web Services project, BPEL project and Composite (JBI) project).
So, in order to run it you have to import it to your Neatbeans (6.7.1 with Glassfish 2.2 and openESB). The port of the Glassfish server should be set to 11893.

After importing the projects:

1> Clean and Build the projects
2> Deploy BookStore2 (WS project) and resolve the missing target server error that you will propably have.
3> Deploy the BookStroreComposite2 project
4> There is a chance that step 3 erased step 2, so if you see that the services are down, do the step 2
	again
5> In the BookStroreComposite2>Test select the test that you want to run and run it.

------------------------------------------------------------------------------------------------
BPEL script is explained in the accompanying PDF file.

------------------------------------------------------------------------------------------------
We have created the following Test cases:
> AuthFail : the user cannot be authenticated, so an error is returned
> BookNotFound : the book that the user is searching for cannot be found, so an error is returned
> BuyFromSwedenAndDoNoShip : the user buys from the publisher 1 and the item do not need to be shiped
> BuyFromUSAandShip : the user buys from the 2ond publisher and the item need to be shipped
> FoundButCannotShip : the user tries to buy, but the area that he lives is not supported by the
	shipping company
