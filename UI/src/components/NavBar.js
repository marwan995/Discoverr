import React, { useState, useEffect, useContext } from "react";
import SearchInput from "./SearchInput";
import { SearchContext } from "../context/SearchContextProvider";
import { useNavigate } from "react-router";
import { Link } from "react-router-dom";
import("@tensorflow/tfjs");
const MobileNet = require("@tensorflow-models/mobilenet");
const NavBar = () => {
    const navigate = useNavigate();
  const { getData, data, isPending, isSuccess, resetSearch } =
    useContext(SearchContext);
  const [searchQuery, setSearchQuery] = useState("");
  const [loadMobileNetModel, setMobileNetModel] = useState(null);


  const handleSubmit = (event) => {
    event.preventDefault();
    getData(searchQuery);
  };

  useEffect(() => {
    resetSearch();
  }, []);
  const loadMobilenetImage = async (src) => {
    return new Promise((resolve, reject) => {
      const img = new Image();
      img.addEventListener("load", () => resolve(img));
      img.addEventListener("error", (err) => reject(err));
      img.src = src;
    });
  };

  useEffect(() => {
    setMobileNetModel(async () => await MobileNet.load());
  }, []);
  const search = (query) => {
    getData(query);
  }
  const handleImageChange = async (e) => {
    let query = "";
    try {
      let image = await loadMobilenetImage(
        URL.createObjectURL(e.target.files[0])
      );
      const predictions = await (await loadMobileNetModel).classify(image);
      for (let prediction of predictions) 
        if (prediction.probability > 0.3)
          query += prediction.className + " ";
      query = query.replace(/,/g, "");
      var array = query.split(" ");
      var uniqueArray = Array.from(new Set(array));
      var result = uniqueArray.join(" ");
      
      getData(result);
    } catch (err) {
      console.log(err);
    }
  };
  useEffect(() => {
    if (!isPending) {
      if (isSuccess) {
        navigate("/results");
      } else {
        console.log("Error");
      }
    }
  }, [isPending, data]);
    return (<div className="NavBar">
        <Link to={"/"}>
        <img src="https://i.ibb.co/h9ybS36/logo2.png" alt="" />
        </Link>
        <form onSubmit={handleSubmit}>
          <SearchInput setSearchQuery={setSearchQuery}  handleImageChange={handleImageChange} search={search}/>
        </form>
    </div>);
}

export default NavBar;