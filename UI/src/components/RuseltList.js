import Quary from "./quary";
import { useContext } from "react";
import { SearchContext } from "../context/SearchContextProvider";

const RuseltList = ({ Quarys, contextQuery }) => {
  const { getData, data, isPending, isSuccess, resetSearch } =useContext(SearchContext);


  return (
    
    <div className="RusltsList">
      {
      Quarys.length===1?<h1>No Result is found for "{contextQuery}"</h1>:
      Quarys.map((Q, index) => (
        <Quary title={Q.title} url={Q.url} des={Q.body} key={index} words={Q.key}/>
      ))}
    </div>
  );
};

export default RuseltList;
