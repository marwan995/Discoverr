import NavBar from "../components/NavBar";
import RuseltList from "../components/RuseltList";
import Pagination from "@mui/material/Pagination";
import { useContext, useEffect, useState } from "react";
import { SearchContext } from "../context/SearchContextProvider";

const Results = () => {
  const {data, Time,contextQuery} = useContext(SearchContext);
  const resultsPerPage = 10; // Change this to set number of results to display per page
  const [currentPage, setCurrentPage] = useState(1);
  const [flitteredData,setflitteredData]=useState([]);
  const handlePageChange = (event, value) => {
    setCurrentPage(value);
  };
  useEffect(()=>{
    setCurrentPage(1);
    data.length==0?setflitteredData(JSON.parse(localStorage.getItem("lastQuery"))):setflitteredData(Array.from(data).filter(e=> e.body.length>e.key.length));
  },[data])

  const startIndex = (currentPage - 1) * resultsPerPage;
  const endIndex = currentPage * resultsPerPage;
  const paginatedResults = flitteredData.slice(startIndex, endIndex+1);

  let numberOfResults = flitteredData.length;
  return (
    <div className="Results">
      <NavBar />
      <h5>About {numberOfResults} results ({Time||"loading"} seconds)</h5>
      <RuseltList Quarys={paginatedResults} contextQuery={contextQuery}/>
      <Pagination
        count={(numberOfResults % resultsPerPage)===0?numberOfResults / resultsPerPage:Math.floor(numberOfResults / resultsPerPage)+1}
        page={currentPage}
        onChange={handlePageChange}
        id="Pagination"
      />
    </div>
  );
};

export default Results;
