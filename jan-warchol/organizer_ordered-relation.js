const mudder = require('mudder');
/* snip */

function prepReorder({relationName, object, newPPtr, insertPos=null}) {
  let objects = getParentInfo(reactiveDbView, relationName, newPPtr).children;
  if (insertPos === null) {
    insertPos = objects.length;
  }
  assert(insertPos <= objects.length);
  const objCurrentIdx = objects.findIndex((o) => o._id === object._id);
  // Inserting an object before itself or before the following object is a noop.
  if (objCurrentIdx != -1 &&
      (insertPos == objCurrentIdx ||
       insertPos == objCurrentIdx + 1)) {
    return [];
  }
  // Filter moved object out of objects list to avoid tricky corner cases.
  if (objCurrentIdx != -1) {
    objects = objects.filter((o) => o._id !== object._id);
    if (objCurrentIdx < insertPos) {
      --insertPos;
    }
  }
  /* snip */

  if (prevSeq != nextSeq) {
    modifiedObj[relationName].seq =
      mudder.base62.mudder(prevSeq, nextSeq, 1)[0];
    return [modifiedObj];
  }

  // Handle duplicated seq.
  const dupedSeq = prevSeq;
  const firstDupIdx = seqs.indexOf(dupedSeq);
  const lastDupIdx = seqs.lastIndexOf(dupedSeq);
  const newSeqsCount = lastDupIdx - firstDupIdx + 2;
  // Note: kZeroSeq and kMaxSeq are never duplicated.
  // TODO: Make distribution non-uniform (leave more space around) and make
  //       sure it encompasses dupedSeq.
  const newSeqs =
    mudder.base62.mudder(seqs[firstDupIdx-1], seqs[lastDupIdx+1], newSeqsCount);
  modifiedObj[relationName].seq = newSeqs[insertPos - firstDupIdx];

  const modifiedObjects = [];
  /* snip */

  // Update seqs in objects after the one inserted.
  for (let i = insertPos; i <= lastDupIdx; ++i) {
    const obj = Object.assign({}, objects[i]);
    obj[relationName].seq = newSeqs[i - firstDupIdx + 1];
    modifiedObjects.push(obj);
  }

  modifiedObjects.push(modifiedObj);
  return modifiedObjects;
}

async function reorderTask(
    {relation, db, task, newPPtr, insertPos=null}) {
  if (relation == 'subtaskOf') {
    delete task.plannedFor
  }
  const tasksToSave = prepReorder({relation, object: task, newPPtr, insertPos});
  for (let taskToSave of tasksToSave) {
    await db.put(taskToSave);
  }
}

/* snip */

module.exports = {
  planTask,
  nestTask,
  reorderTask,
  prepareReorder,
};
