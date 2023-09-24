import React, { useState, useEffect } from "react";
import Earth from "../components/Earth";
import Search from "../components/Search";
const Home = () => {
  const [speed, setSpeed] = useState(5);

  const startSearchAnimation = () => {
    setSpeed(80);
  };

  const stopSearchAnimation = () => {
    setSpeed(5);
  };

  return (
    <div className="container-background">
      <div className="Container">
        <Search
          startSearchAnimation={startSearchAnimation}
          stopSearchAnimation={stopSearchAnimation}
        />
        <Earth speed={speed} />
      </div>
    </div>
  );
};

export default Home;
