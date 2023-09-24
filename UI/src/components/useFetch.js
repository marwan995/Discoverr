import { useEffect, useState } from "react";
import axios from "axios";

let useFetch = (url) => {
  const [data, setBlogs] = useState([]);
  const [isPending, setPending] = useState(true);

  const getData = async (url) => {
    const res = await axios.get(url);
    return res;
  };

  useEffect(() => {
    let res = getData(url);
    setBlogs(res.data);
    setPending(false);
  }, [url]); //[] if u want to rander once or put the name of the var u want to trace
  return { data, isPending };
};

export default useFetch;
