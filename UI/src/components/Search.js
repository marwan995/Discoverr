import React, { useState, useEffect, useContext } from "react";
import SearchInput from "./SearchInput";
import { SearchContext } from "../context/SearchContextProvider";
import { useNavigate } from "react-router";
import "@tensorflow/tfjs";
const MobileNet = require("@tensorflow-models/mobilenet");

const Search = ({ startSearchAnimation, stopSearchAnimation }) => {
  const navigate = useNavigate();
  const { getData, data, isPending, isSuccess, resetSearch, setPending } =
    useContext(SearchContext);
  const [searchQuery, setSearchQuery] = useState("");
  const [loadMobileNetModel, setMobileNetModel] = useState(null);

  const handleSubmit = (event) => {
    event.preventDefault();
    startSearchAnimation();
    getData(searchQuery);
  };

  const search = (query) => {
    startSearchAnimation();
    getData(query);
  }

  useEffect(() => {
    resetSearch();
  }, []);

  useEffect(() => {
    setMobileNetModel(async () => await MobileNet.load());
  }, []);

  const loadMobilenetImage = async (src) => {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.addEventListener("load", () => resolve(img));
      img.addEventListener("error", (err) => reject(err));
      img.src = src;
    });
  };

  const handleImageChange = async (e) => {
    let query = "";
    try {
      let image = await loadMobilenetImage(
        URL.createObjectURL(e.target.files[0])
      );
      const predictions = await (await loadMobileNetModel).classify(image);
      for (let prediction of predictions) 
        if (prediction.probability > 0.3)query += prediction.className + " ";
      query = query.replace(/,/g, "");
      var array = query.split(" ");
      var uniqueArray = Array.from(new Set(array));
      var result = uniqueArray.join(" ");
      startSearchAnimation();
      getData(result);
    } catch (err) {
      console.log(err);
    }
  };

  useEffect(() => {
    if (!isPending) {
      stopSearchAnimation();
      if (isSuccess) {
        setPending(false);
        navigate("/results");
      } else {
        console.log("Error");
      }
    }
  }, [isPending, data]);

  return (
    <div className="Search">
      <img src="logo.png" alt="" />
      <div>
        <h1>Discoverr</h1>
        <h3>
          Welcome to Discoverr, a powerful search engine that helps you find
          what you're looking for quickly and easily. Our search engine uses
          advanced web crawling and indexing technology to discover and organize
          the most relevant content on the internet ,so you can find the
          information you need without having to sift through pages of
          irrelevant results. Our crawler continually scans the web for new and
          updated content, while our indexer analyzes and categorizes this
          information to make it easily searchable.
        </h3>
        <form onSubmit={handleSubmit}>
          <SearchInput 
          setSearchQuery={setSearchQuery} 
          handleImageChange={handleImageChange}
          search={search}
          />
        </form>
      </div>
    </div>
  );
};

export default Search;
