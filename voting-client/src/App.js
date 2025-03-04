import React, { useState } from "react";

function App() {
  const [vote, setVote] = useState("");
  const [chain, setChain] = useState([]);

  // Funcție pentru trimiterea votului către backend
  const submitVote = async () => {
    if (!vote) {
      alert("Introduceți un vot valid!");
      return;
    }

    try {
      // Trimitem votul către endpoint-ul /vote
      const response = await fetch("http://localhost:8080/vote", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ vote }),
      });

      if (!response.ok) {
        throw new Error("Eroare la trimiterea votului!");
      }

      const data = await response.json();
      alert(data.message);
      setVote(""); // Resetăm câmpul de input după trimitere
    } catch (error) {
      console.error("Vote submission error:", error);
      alert("Eroare la trimiterea votului!");
    }
  };

  // Funcție pentru obținerea blockchain-ului curent de la backend
  const fetchChain = async () => {
    try {
      const response = await fetch("http://localhost:8080/chain");

      if (!response.ok) {
        throw new Error("Eroare la obținerea blockchain-ului!");
      }

      const data = await response.json();
      setChain(data);
    } catch (error) {
      console.error("Error fetching chain:", error);
      alert("Eroare la obținerea blockchain-ului!");
    }
  };

  return (
    <div style={{ padding: "2rem", fontFamily: "Arial" }}>
      <h1>Voting App</h1>
      
      <input
        type="text"
        value={vote}
        onChange={(e) => setVote(e.target.value)}
        placeholder="Introduceți votul (ex: Alice, Bob, Da, Nu)"
      />
      <button onClick={submitVote}>Submit Vote</button>

      <hr />
      
      <button onClick={fetchChain}>View Blockchain</button>
      <pre>{JSON.stringify(chain, null, 2)}</pre>
    </div>
  );
}

export default App;
