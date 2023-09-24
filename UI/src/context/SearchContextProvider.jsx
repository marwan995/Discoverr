import React, { createContext, useState } from "react";
import axios from "axios";

export const SearchContext = createContext();

export default function SearchContextProvider(props) {
  const [data, setBlogs] = useState([]);
  const [isPending, setPending] = useState(true);
  const [isSuccess, setSuccess] = useState(false);
  const [Time,setTime]=useState(0);
  const [contextQuery, setContextQuery] = useState("")

  const getData = async (query) => {
    resetSearch();
    try {
      query=query.replace(/"/g, "'");
      setContextQuery(query);
      const res = await axios.get(`http://localhost:8080/search?q=${query}`);
      try
      {
      localStorage.setItem("lastQuery", JSON.stringify(res.data));
      }
      catch(e){
        console.log("error");
      }
      setTime(res.data.pop().time.toFixed(2))
      console.log(res.data);
      setBlogs(res.data);
      setSuccess(true);
    } catch (err) {
      setSuccess(false);
    }
    setPending(false);
  };

  const resetSearch = () => {
    setPending(true);
    setSuccess(false);
  };
    return (
    <SearchContext.Provider value={{Time,getData, data, isPending, isSuccess, resetSearch,contextQuery,setPending }}>
      {props.children}
    </SearchContext.Provider>
  );
}
