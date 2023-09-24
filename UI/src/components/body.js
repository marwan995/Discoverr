import React from "react";

function HighlightWords({ sentence, words }) {

  let Bodywords = sentence?sentence.toLowerCase():"";
  if(words&&words.length>0){
  words.forEach(e => {
    let regex = new RegExp('\\b' + e + '\\b', 'gi');
    Bodywords=Bodywords.replaceAll(regex,`<strong>${e}</strong>`)
  });
}
  return <p dangerouslySetInnerHTML={{ __html: Bodywords+"..." }}></p>;

}

export default HighlightWords;
