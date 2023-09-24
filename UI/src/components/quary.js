import { Link } from "react-router-dom";
import HighlightWords from  "./body"
const Quary = ({ title, url, des,words }) => {
  return (
    <div className="quary">
      <Link to={`${url}`}>
        <h2> {title||"No title is found"} </h2>
      </Link>
      <h4>{url}</h4>
      <HighlightWords sentence={des} words={words} />
    </div>
  );
};

export default Quary;
